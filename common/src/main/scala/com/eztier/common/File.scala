package com.eztier.common

import cats.implicits._
import cats.effect.{Blocker, ContextShift, Sync}
import fs2.{Stream, Pipe}
import fs2.text.{lines, utf8Decode, utf8Encode}
import java.nio.file.Paths

import Util.WrapThrowable

object File {

  def readAllFromFile[F[_]: Sync : ContextShift](blocker: Blocker)(path: String, bufferSize: Int = 8192): F[String] =
    for {
      str <- fs2.io.file.readAll(Paths.get(path), blocker, bufferSize)
        .through(utf8Decode)
        .handleErrorWith { e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.emit(ex)
        }
        .compile
        .toList
        .flatMap(a => a.mkString("").pure[F])
    } yield str

  def readByLinesFromFile[F[_]: Sync : ContextShift](blocker: Blocker)(path: String, bufferSize: Int = 8192): Stream[F, String] =
    for {
      str <- fs2.io.file.readAll(Paths.get(path), blocker, bufferSize)
        .through(utf8Decode)
        .through(lines)
        .handleErrorWith { e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.emit(ex)
        }
    } yield str

  def writeWithLinesToFile[F[_]: Sync : ContextShift](blocker: Blocker)(path: String): Pipe[F, String, Unit] =
    _.intersperse("\n")
      .through(utf8Encode)
      .through(fs2.io.file.writeAll(Paths.get(path), blocker))

}
