package com.eztier
package testfs2cassandra.infrastructure

import cats.implicits._
import fs2.Stream
import cats.effect.{Async, ConcurrentEffect}
import datasource.infrastructure.http.HttpSession

class HttpInterpreter[F[_]: Async : ConcurrentEffect] {
  private val session = HttpSession[F]()

  def runConcurrentTest(src: Stream[F, Int]): Stream[F, Unit] =
    src
    .mapAsyncUnordered(4)(_ => session.getF)
    .chunkN(2)
      .flatMap {
        a =>
          println(a.size)
          a.foreach(println(_))

          Stream.eval(().pure[F])
      }
}
