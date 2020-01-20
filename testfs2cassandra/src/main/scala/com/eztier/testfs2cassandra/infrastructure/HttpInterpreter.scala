package com.eztier
package testfs2cassandra.infrastructure

import cats.implicits._
import fs2.Stream
import cats.effect.{Async, ConcurrentEffect}
import datasource.infrastructure.http.HttpSession
import testfs2cassandra.config._
import testfs2cassandra.domain._

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

class DocumentHttpInterpreter[F[_]: Async : ConcurrentEffect](conf: HttpConfig)
  extends DocumentXmlRepo[F] {

  private val session = HttpSession[F]()

  override def fetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document] = {
    val body = ""

    src
      .mapAsyncUnordered(4){ r =>
        session.postWithBody(conf.url, body).value
          .flatMap {
            case Right(a) => Document(r._1.some, r._2.some, a.some).pure[F]
            case Left(e) => Document(r._1.some, r._2.some, (<error>${e}</error>).toString().some).pure[F]
          }
      }
  }
}
