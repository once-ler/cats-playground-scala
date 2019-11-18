package com.eztier.clickmock
package test

import org.specs2.mutable._
import infrastructure._
import java.util.concurrent.Executors

import cats.Applicative
import cats.effect.{Async, ConcurrentEffect, ContextShift, IO, Resource, Sync}
import com.eztier.clickmock.config.{AppConfig, SoapConfig}
import io.circe.config.{parser => ConfigParser}

import scala.concurrent.ExecutionContext
import io.circe.Decoder
import io.circe.generic.semiauto._
import fs2.Stream
import cats.effect._
import fs2.text.utf8Decode
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import soapenvelope12.Envelope

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

        action.fold("") {
          sa =>
            val soapAction = sa.split('/').last

            soapAction match {
              case "Login" =>
            }

            ""
        }




        val response = scala.xml.XML.loadString(m)
        val xml = scalaxb.fromXML[Envelope](response)

        // val login = scalaxb.fromXML[com.eztier.clickmock.soap.entity.Login](response)

        /*
        val obj = scalaxb.fromXML[com.eztier.clickmock.soap.entity.Foo](node)
        val document = scalaxb.toXML[com.eztier.clickmock.soap.entity.Foo](obj, "foo", com.eztier.clickmock.soap.entity.defaultScope)
        */

        F.pure(Response(status = Status.Ok)
          .withContentType(`Content-Type`(MediaType.text.xml))
          .withEntity(m)
        )
      }

      /*
      val b = req.body.through(utf8Decode)

      b.flatMap { s =>
        println(s)

        Stream.eval(s)
      }
      */

/*
      val body = s"""<LoginResponse xmlns="http://clickcommerce.com/Extranet/WebServices"><LoginResult>1234</LoginResult></LoginResponse>"""
      val xml = s"""<?xml version="1.0"?><soap:Envelope
xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
soap:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><soap:Body>$body</soap:Body></soap:Envelope>"""

      F.pure(Response(status = Status.Ok)
        .withContentType(`Content-Type`(MediaType.text.xml))
        .withEntity(xml)
      )
 */

    }
  }

  def helloWorldService[F[_]: Effect]: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" / name =>
      Effect[F].pure(Response(Status.Ok).withEntity(s"Hello, $name."))
  }

  def createHttpServer[F[_]: Effect : ConcurrentEffect : Timer] = {
    val httpApp = Router("/" -> helloWorldService[F], "/api" -> rpcService[F]()).orNotFound

    /*
    blockingThreadPool.use {
      ec =>
        val serverBuilder = BlazeServerBuilder[F]
          .bindHttp(8080, "localhost")
          .withHttpApp(httpApp)
          .withExecutionContext(ec)

        Applicative[F].pure(serverBuilder)
    }
    */

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

      /*
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      implicit val cs = IO.contextShift(ec)
      implicit val timer = IO.timer(ec)
      */
      val backgroundThread = createHttpServer[IO].resource.use(_ => IO.never).start.unsafeRunSync()

      val resources = createMockService[IO]

      resources.use{
        mock =>

          val a = Stream.eval(mock.tryGetEntityByID(Some("123"))).compile.toList
          val b = a.unsafeRunSync()

          IO(println("Start"))
      }.unsafeRunSync()

      1 mustEqual 1
    }
  }
}
