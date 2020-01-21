package com.eztier
package testfs2cassandra.infrastructure

import java.net.URI

import cats.implicits._
import fs2.Stream
import cats.effect.{Async, ConcurrentEffect}
import datasource.infrastructure.http.HttpSession
import org.http4s.{Header, Headers}
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

  // private val uriInfo = new URI(conf.url)

  private def testBody =
    """<?xml version="1.0" encoding="utf-8"?>
      |<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      |  <soap:Body>
      |    <FahrenheitToCelsius xmlns="https://www.w3schools.com/xml/">
      |      <Fahrenheit>75</Fahrenheit>
      |    </FahrenheitToCelsius>
      |  </soap:Body>
      |</soap:Envelope>
    """.stripMargin

  private val testHeaders = Headers.of(Header("Content-Type", "text/xml"), Header("SOAPAction", "https://www.w3schools.com/xml/FahrenheitToCelsius"))

  private def testFetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document] = {
    src
      .mapAsyncUnordered(4){ r =>
        session.postWithBody(conf.url, testBody, testHeaders).value
          .flatMap {
            case Right(a) => Document(r._1.some, r._2.some, a.some).pure[F]
            case Left(e) => Document(r._1.some, r._2.some, (<error>{e.getMessage}</error>).toString().some).pure[F]
          }
      }
  }

  override def fetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document] = {
    val body = ""

    val headers = Headers.empty

    testFetchDocumentXml(src)

    /*
    src
      .mapAsyncUnordered(4){ r =>
        session.postWithBody(conf.url, body).value
          .flatMap {
            case Right(a) => Document(r._1.some, r._2.some, a.some).pure[F]
            case Left(e) => Document(r._1.some, r._2.some, (<error>${e}</error>).toString().some).pure[F]
          }
      }

     */
  }
}
