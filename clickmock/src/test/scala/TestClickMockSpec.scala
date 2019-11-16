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
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router

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
    case POST -> Root / "soap" => {

      val body = <html><h1>h1</h1></html>
      val xml = s"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>$body"""

      F.pure(Response(status = Status.BadRequest)
        .withContentType(`Content-Type`(MediaType.text.xml))
        .withEntity(xml)
      )
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
