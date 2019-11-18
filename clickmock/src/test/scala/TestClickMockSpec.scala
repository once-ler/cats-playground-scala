package com.eztier.clickmock
package test

import org.specs2.mutable._
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.config.{parser => ConfigParser}

import cats.effect._

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router

import fs2.Stream

import com.eztier.clickmock.config.{AppConfig, SoapConfig}
import com.eztier.clickmock.soap.entity._
import soap._
import infrastructure._

object config {
  implicit val appDecoder: Decoder[AppConfig] = deriveDecoder
  implicit val soapDecoder: Decoder[SoapConfig] = deriveDecoder
}

object FauxWeb {
  case class SomeXmlResponse(xml: String)

  implicit def oneXmlEncoder[F[_]]: EntityEncoder[F, SomeXmlResponse] = ???
  implicit def manyXmlEncoder[F[_]]: EntityEncoder[F, Seq[SomeXmlResponse]] = ???

  trait RpcRepo[F[_]] {
    def process(request: String): F[Option[SomeXmlResponse]]
  }

  def rpcService[F[_]](repo: Option[RpcRepo[F]] = None)(implicit F: Effect[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "soap" => {

      req.decode[String] { m =>

        val action = for {
          c <- req.headers.get(`Content-Type`)
          a <- c.mediaType.extensions.get("action")
        } yield a

        val body = action.fold("") {
          sa =>
            val soapAction = sa.split('/').last

            soapAction match {
              case "Login" =>
                val obj = LoginResponse(Some("abc123"))
                val loginResponse = scalaxb.toXML[LoginResponse](obj, "LoginResponse", com.eztier.clickmock.soap.entity.defaultScope)
                loginResponse.toString()
              case "getEntityByID" =>
                val obj = GetEntityByIDResponse(Some("<mainspan />"))
                val getEntityByIDResponse = scalaxb.toXML[GetEntityByIDResponse](obj, "getEntityByIDResponse", entity.defaultScope)
                getEntityByIDResponse.toString()
              case "redefineEntityByID" =>
                val obj = RedefineEntityByIDResponse(Some("<mainspan />"))
                val redefineEntityByIDResponse = scalaxb.toXML[RedefineEntityByIDResponse](obj, "redefineEntityByIDResponse", entity.defaultScope)
                redefineEntityByIDResponse.toString()
              case "createEntity" =>
                val obj = CreateEntityResponse(Some("<mainspan />"))
                val createEntityResponse = scalaxb.toXML[CreateEntityResponse](obj, "createEntityResponse", entity.defaultScope)
                createEntityResponse.toString()
              case "Logoff" =>
                val obj = LogoffResponse()
                val logoffResponse = scalaxb.toXML[LogoffResponse](obj, "LogoffResponse", entity.defaultScope)
                logoffResponse.toString()
              case _ =>
                import soapenvelope12._

                val obj = soapenvelope12.Fault(
                  Faultcode(FaultcodeEnum.fromString("BS", entity.defaultScope)),
                  Faultreason(Reasontext("Badness", "en-US"))
                )

                scalaxb.toXML[Fault](obj, "Fault", entity.defaultScope).toString()
            }
        }

        val xml = s"""<?xml version="1.0"?><soap:Envelope
xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
soap:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><soap:Body>$body</soap:Body></soap:Envelope>"""

        F.pure(Response(status = Status.Ok)
          .withContentType(`Content-Type`(MediaType.text.xml))
          .withEntity(xml)
        )

      }

    }
  }

  def helloWorldService[F[_]: Effect]: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" / name =>
      Effect[F].pure(Response(Status.Ok).withEntity(s"Hello, $name."))
  }

  def createHttpServer[F[_]: Effect : ConcurrentEffect : Timer] = {
    val httpApp = Router("/" -> helloWorldService[F], "/api" -> rpcService[F]()).orNotFound

    val serverBuilder = BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)

    serverBuilder

    // Run in background
    // val fiber = serverBuilder.resource.use(_ => IO.never).start.unsafeRunSync()
  }

}

class TestClickMockSpec[F[_]] extends Specification {
  val ec = scala.concurrent.ExecutionContext.global
  implicit val timer = IO.timer(ec)
  implicit val cs = IO.contextShift(ec)  // Need cats.effect.ContextShift[cats.effect.IO] because not inside of IOApp

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createMockService[F[_]: Async: ContextShift: ConcurrentEffect: Timer] = {
    import config._

    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("clickmock")) // Lifts an applicative into a resource.
      b = blockingThreadPool[F]
      ms = CkMockService(conf, b)
    } yield ms
  }

  "CkMockService" should {
    "Initialize" in {

      // Make sure to change the VM parameters:
      // -Dclickmock.soap.url=http://localhost:8080/api/soap

      import FauxWeb._

      // Server
      val fiberThread = createHttpServer[IO].resource.use(_ => IO.never).start.unsafeRunSync()

      // Client
      val resources = createMockService[IO]

      resources.use{
        mock =>

          val a = Stream.eval(mock.tryGetEntityByID(Some("123"))).compile.toList
          val b = a.unsafeRunSync()

          IO(println(b))
      }.unsafeRunSync()

      1 mustEqual 1
    }
  }
}
