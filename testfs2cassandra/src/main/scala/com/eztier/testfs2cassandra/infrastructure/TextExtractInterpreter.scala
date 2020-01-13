package com.eztier.testfs2cassandra
package infrastructure

import java.util.concurrent.{Executors, ThreadFactory}
import java.util.concurrent.atomic.AtomicLong

import cats.implicits._
import cats.Show
import cats.effect.concurrent.Semaphore
import cats.effect.{Async, Blocker, Concurrent, ConcurrentEffect, ContextShift, Sync}
import fs2.{Pipe, Stream}

import scala.concurrent.ExecutionContext
import domain.Extracted

import scala.collection.mutable.ArrayBuffer

class PoolWorker extends Thread {
  override def run(): Unit = super.run()
}

class TextExtractInterpreter[F[_]: Async :ContextShift :ConcurrentEffect](s: Semaphore[F]) {

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

  // private val textExtractor = TextExtractor()

  private val busy = scala.collection.mutable.Queue.empty[TextExtractor]
  private val available = scala.collection.mutable.Queue.empty[TextExtractor]

  def initialize(workerCount: Int) = {
    // available ++= ArrayBuffer(TextExtractor(), TextExtractor(), TextExtractor())
    (1 to workerCount).foreach(l => available += TextExtractor())
    this
  }

  private def processFile(filePath: String) = {
    println(Thread.currentThread().getName())
    for {
      x <- s.available
      _ <- Sync[F].delay(println(s"$filePath >> Availability: $x"))
      _ <- s.acquire
      y <- s.available
      _ <- Sync[F].delay(println(s"$filePath >> Started | Availability: $y"))
      textExtractor = available.dequeue()
      r <- Sync[F].delay(textExtractor.extract(filePath))
      _ <- Sync[F].delay(available.enqueue(textExtractor))
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
            case Some(o) => Some(o.copy(id = in._1))
            case _ => None
          }
        }
  }

  def extract = (in: (String, String)) =>
    Sync[F]
      .suspend(processFile(in._2))
      .map { e =>
        e match {
          case Some(o) => Some(o.copy(id = in._1))
          case _ => None
        }
      }

  def aggregate(src: Stream[F, (String, String)]) = {
    implicit val showPerson: Show[Option[Extracted]] = Show.show(d => d match {
      case Some(o) => s"${o.content}"
      case None => "Nothing"
    })

    val concurrency = 5

    src
      .mapAsyncUnordered(concurrency)(extract)
      // .through(toExtractPipeS)
      .covary[F]
      .showLinesStdOut
  }

  /*
  def compute(files: List[Tuple2[String, String]]): Stream[F, Option[Extracted]] = {
    val src = Stream.emits(files)
    val concurrency = 4

    src.evalMap{
      row =>
        Sync[F].delay {
          val r = for {
            e <- processFile(row._2)
          } yield e.copy(id = row._1)

          Stream.emit(r)
        }
      }
      .parJoin(concurrency)
  }
  */
}
