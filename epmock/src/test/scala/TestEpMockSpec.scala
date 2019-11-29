package com.eztier.epmock
package test

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Effect, Sync, Timer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import fs2.{Pipe, Stream}
import org.specs2.mutable._
import infrastructure.file.FilePatientRepositoryInterpreter
import javax.xml.stream.XMLEventReader

import scala.xml.Elem

object FauxWeb {

  def rpcService[F[_]: Sync : ContextShift](implicit F: Effect[F]): HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "xml" => {
      req.decode[String] { m =>

        import com.scalawilliam.xs4s.Implicits._

        val patientXmlFileInterpreter = FilePatientRepositoryInterpreter[F]

        val xmlEventReader: XMLEventReader = patientXmlFileInterpreter.fectchXmlFile

        val body = Stream
          .fromIterator(xmlEventReader.toIterator)
          .evalMap(a => Applicative[F].pure(a.toString))
          .through(fs2.text.utf8Encode)

        // type EntityBody[F] = Stream[F, Byte]
        F.pure(Response(status = Status.Ok)
          .withContentType(`Content-Type`(MediaType.text.xml))
          .withBodyStream(body)
        )

      }
    }

  }

  def createHttpServer[F[_]: Effect : ConcurrentEffect : Timer: ContextShift] = {
    val httpApp = Router("/api" -> rpcService[F]).orNotFound

    val serverBuilder = BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)

    serverBuilder
  }

}

class TestEpMockSpec extends Specification {

}
