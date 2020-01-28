package com.eztier.testfs2cassandra
package infrastructure

import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicLong

import cats.implicits._
import cats.Show
import cats.effect.concurrent.Semaphore
import cats.effect.{Async, Blocker, Concurrent, ConcurrentEffect, ContextShift, Sync}
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.ExecutionContext

// import shapeless.LabelledGeneric

import domain._

class TextExtractInterpreter[F[_]: Async :ContextShift :ConcurrentEffect](concurrency: Int, s: Semaphore[F], cassandraInterpreter: CassandraInterpreter[F]) {

  implicit val showPerson: Show[Option[Extracted]] = Show.show(d => d match {
    case Some(o) => s"${o.content}"
    case None => "Nothing"
  })

  // Subset of cassandra case class used for persistence.
  // private val genericExtractedSubset = LabelledGeneric[Extracted]

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

  private val workers = scala.collection.mutable.Queue.empty[TextExtractor]

  def initialize = {
    // workers ++= ArrayBuffer(TextExtractor(), TextExtractor(), TextExtractor())
    (1 to concurrency).foreach(l => workers += TextExtractor())
    this
  }

  private def processFile(filePath: String) = {
    for {
      // x <- s.available
      // _ <- Sync[F].delay(println(s"$filePath >> Availability: $x"))
      _ <- s.acquire
      // y <- s.available
      // _ <- Sync[F].delay(println(s"$filePath >> Started | Availability: $y"))
      // _ <- Sync[F].delay(println(Thread.currentThread().getName()))
      textExtractor = workers.dequeue()
      r <- Sync[F].delay(textExtractor.extract(filePath))
      _ <- Sync[F].delay(workers.enqueue(textExtractor))
      _ <- s.release
      // z <- s.available
      // _ <- Sync[F].delay(println(s"$filePath >> Done | Availability: $z"))
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
      .suspend(processFile(in.doc_file_path.get))
      .map { e =>
        e match {
          case Some(o) => Some(o.copy(doc_id = in.doc_id))
          case _ => None
        }
      }

  private def extract = (in: DocumentMetadata) =>
    Sync[F]
      .suspend(processFile(in.doc_file_path.get))
      .map { e =>
        e match {
          case Some(o) => Some(o.copy(doc_id = in.doc_id))
          case _ => None
        }
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

  def extractChunkDocument(c: Chunk[DocumentMetadata]) = {

    val src = Stream.emits(c.toVector).covary[F]

    src.mapAsyncUnordered(concurrency)(extract)

  }

}
