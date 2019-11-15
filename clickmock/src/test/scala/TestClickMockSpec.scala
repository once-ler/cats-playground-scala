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

object config {
  implicit val appDecoder: Decoder[AppConfig] = deriveDecoder
  implicit val soapDecoder: Decoder[SoapConfig] = deriveDecoder
}


class TestClickMockSpec[F[_]] extends Specification {
  val ec = scala.concurrent.ExecutionContext.global
  implicit val timer = IO.timer(ec)
  implicit val cs = IO.contextShift(ec)  // Need cats.effect.ContextShift[cats.effect.IO] because not inside of IOApp

  def blockingThreadPool(implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createMockService[F[_]: Async: ContextShift: ConcurrentEffect] = {
    import config._

    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("clickmock")) // Lifts an applicative into a resource.
      b = blockingThreadPool
      ms = CkMockService(conf, b)
    } yield ms
  }

  "CkMockService" should {
    "Initialize" in {

      val ms = createMockService[IO]

      1 mustEqual 1
    }
  }
}
