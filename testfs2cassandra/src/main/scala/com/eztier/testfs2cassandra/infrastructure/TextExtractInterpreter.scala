package com.eztier.testfs2cassandra
package infrastructure

import java.util.concurrent.Executors

import cats.implicits._
import cats.Show
import cats.effect.{Async, Blocker, Concurrent, ConcurrentEffect, ContextShift, Sync}
import fs2.{Pipe, Stream}

import scala.concurrent.ExecutionContext
import domain.Extracted

class TextExtractInterpreter[F[_]: Async :ContextShift :ConcurrentEffect] {
  private implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  private implicit val blocker = Blocker.liftExecutionContext(ec)

  private def processFile(filePath: String) = {
    TextExtractor().extract(filePath)
  }

  def toExtractPipeS: Pipe[F, (String, String), Option[Extracted]] = _.evalMap {
    in =>
      Sync[F].delay {
        for {
          e <- processFile(in._2)
        } yield e.copy(id = in._1)
      }
  }

  def extract = (in: (String, String)) =>
    Sync[F].delay {
      for {
        e <- processFile(in._2)
      } yield e.copy(id = in._1)
    }

  def aggregate(src: Stream[F, (String, String)]) = {
    implicit val showPerson: Show[Option[Extracted]] = Show.show(d => d match {
      case Some(o) => s"${o.content}"
      case None => "Nothing"
    })

    val concurrency = 4

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
