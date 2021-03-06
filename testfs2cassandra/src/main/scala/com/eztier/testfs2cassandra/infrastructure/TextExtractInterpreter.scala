package com.eztier
package testfs2cassandra.infrastructure

import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicLong

import cats.implicits._
import cats.Show
import cats.effect.concurrent.Semaphore
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Sync}
import fs2.{Chunk, Pipe, Stream}
import io.chrisdavenport.log4cats.Logger
import scala.concurrent.ExecutionContext
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

import common.CatsLogger._
import common.Util._
import common.mergeSyntax._
import testfs2cassandra.domain._

class TextExtractInterpreter[F[_]: Async :ContextShift :ConcurrentEffect](concurrency: Int, s: Semaphore[F], pathPrefix: Option[String] = None)
  extends DocumentExtractRepo[F] {

  implicit val showPerson: Show[Option[Extracted]] = Show.show(d => d match {
    case Some(o) => s"${o.content}"
    case None => "Nothing"
  })

  private implicit val ec = ExecutionContext
    .fromExecutorService(
      Executors.newFixedThreadPool(8, new ThreadFactory {
        private val counter = new AtomicLong(0L)

        def newThread(r: Runnable) = {
          val th = new Thread(r)
          th.setName("tika-" + counter.getAndIncrement.toString)
          th.setDaemon(true)
          th
        }
      })
    )

  private implicit val blocker = Blocker.liftExecutionContext(ec)

  // Commons Pool
  private var pool: TextExtractorPool = null

  def initialize = {
    val factory = new TextExtractorFactory()
    val config = new GenericObjectPoolConfig[TextExtractor]()
    config.setMaxIdle(concurrency)
    config.setMaxTotal(concurrency)
    config.setTestOnBorrow(true)
    config.setTestOnReturn(true)

    pool = new TextExtractorPool(factory, config)

    this
  }

  private def processFile(filePath: String) = {
    for {
      x <- s.available
      _ <- Sync[F].delay(println(s"$filePath >> Availability: $x"))
      _ <- s.acquire
      y <- s.available
      _ <- Sync[F].delay(println(s"$filePath >> Started | Availability: $y"))
      _ <- Sync[F].delay(println(Thread.currentThread().getName()))
      textExtractor = pool.borrowObject()
      r <- Sync[F].delay(textExtractor.extract(filePath))
        .handleErrorWith {
          e =>
            val ex = WrapThrowable(e).printStackTraceAsString
            val noop: Option[Extracted] = None

            for {
              _ <- Logger[F].error(ex)
            } yield noop
        }
      _ <- Sync[F].delay(pool.returnObject(textExtractor))
      _ <- s.release
      z <- s.available
      _ <- Sync[F].delay(println(s"$filePath >> Done | Availability: $z"))
    } yield r

  }

  def toExtractPipeS: Pipe[F, (String, String), Option[Extracted]] = _.evalMap {
    in =>
      Sync[F]
        .suspend(processFile(in._2))
        .map { e =>
          e match {
            case Some(o) => Some(o.copy(doc_id = Some(in._1)))
            case _ => None
          }
        }
  }

  private def extract2 = (in: Extracted) =>
    Sync[F]
      .suspend(processFile(pathPrefix.getOrElse("") + in.doc_file_path.getOrElse("")))
      .map { e =>
        e match {
          case Some(o) => Some(o.copy(doc_id = in.doc_id))
          case _ => None
        }
      }

  private def extract3 = (in: DocumentMetadata) =>
    Sync[F]
      .suspend(processFile(pathPrefix.getOrElse("") + in.doc_file_path.getOrElse("")))
      .map { e =>
        e match {
          case Some(o) => Some(o.copy(doc_id = in.doc_id))
          case _ => None
        }
      }

  private def extract: DocumentMetadata => F[Option[DocumentExtracted]] = (in: DocumentMetadata) =>
    Sync[F]
      .suspend(processFile(pathPrefix.getOrElse("") + in.doc_file_path.getOrElse("")))
      .map { e =>
        e match {
          case Some(e) =>
            // Merge DocumentMetadata to DocumentExtracted
            val de = DocumentExtracted()
            val merged: DocumentExtracted = de merge in
            val mergedWithContent = merged.copy(content = e.content, metadata = e.metadata)

            Some(mergedWithContent)
          case _ => None
        }
      }
      .handleErrorWith {
        e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          val noop: Option[DocumentExtracted] = None

          for {
            _ <- Logger[F].error(s"${in.id.getOrElse("")}: ${ex}")
          } yield noop
      }


  private def persist(chunk: Chunk[Option[Extracted]]): F[Unit] =
    Async[F].async { callback =>
      println(s"Writing batch of ${chunk.size} to database by ${Thread.currentThread().getName}")
      callback(Right(()))
    }

  def aggregate(src: Stream[F, Extracted]) = {

    src
      .mapAsyncUnordered(concurrency)(extract2)
      .chunkN(100)
      .parEvalMapUnordered(100)(persist)
      // .through(toExtractPipeS)
      .covary[F]
      .showLinesStdOut
  }

  //

  override def extractDocument(src: Stream[F, DocumentMetadata]): Stream[F, Option[DocumentExtracted]] =
    src.mapAsyncUnordered(concurrency)(extract)

  def extractChunkDocument(c: Chunk[DocumentMetadata]) = {

    val src = Stream.emits(c.toVector).covary[F]

    src.mapAsyncUnordered(concurrency)(extract)

  }

}
