package com.eztier.epmock
package test

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Effect, IO, Sync, Timer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import fs2.{Pipe, Stream}
import org.specs2.mutable._
import infrastructure.file.FilePatientRepositoryInterpreter
import infrastructure.http.HttpPatientRepositoryInterpreter
import io.circe.generic.extras.Configuration
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

  val ec = scala.concurrent.ExecutionContext.global
  implicit val timer = IO.timer(ec)
  implicit val cs = IO.contextShift(ec)  // Need cats.effect.ContextShift[cats.effect.IO] because not inside of IOApp

  "EpMockService" should {
    "Create faux web" in {

      import FauxWeb._

      // Server
      val fiberThread = createHttpServer[IO].resource.use(_ => IO.never).start.unsafeRunSync()

      val httpPatientRepositoryInterpreter = new HttpPatientRepositoryInterpreter[IO]

      val a = httpPatientRepositoryInterpreter.fetchPatients()

      1 mustEqual 1
    }
  }

  "List[EpPatient]" should {
    "Should sort in desc order" in {

      val s = new FilePatientRepositoryInterpreter[IO]

      val s1 = s.fetchPatients.compile.toList.unsafeRunSync()

      val s2 = s1
        .filter(a => a.Mrn.isDefined)
        .groupBy(_.Mrn.get)
        .map(d => d._2.sortBy(b => - b.dateCreated.get).head)

      1 mustEqual 1
    }
  }

  "EpQuery" should {
    "Serialize correctly" in {
      import domain._

      import io.circe.syntax._

      val epQuery = EpQuery()

      val j = epQuery.asJson

      1 mustEqual 1
    }
  }

}
