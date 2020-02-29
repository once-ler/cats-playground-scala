package com.eztier
package test.common

import cats.implicits._
import cats.{Applicative, Monad, Semigroup, Semigroupal}
import cats.data.Chain
import cats.effect.{IO, Resource, Sync}
import cats.mtl.MonadState
import cats.mtl.instances.state._
import fs2.Stream
import org.specs2.mutable._
import common._

class TestMonadLog extends Specification {

  class SafelyDoSomethingService[F[_]: Monad : Applicative : Sync: MonadLog[?[_], Chain[String]]](i: Int) {
    val logs = implicitly[MonadLog[F, Chain[String]]]

    def doIt =
      for {
        a <- Sync[F].delay(println("Start"))
        _ <- logs.log(Chain.one(s"Starting ${i}"))
        _ <- logs.log(Chain.one(s"Starting ${i+5}"))
        _ <- logs.flush { c =>
          // Do something with the logs.
          println(c)
          Applicative[F].pure(())
        }
      } yield ()
  }

  class AggregateDoSomething[F[_]: Monad : Applicative : Sync](a: SafelyDoSomethingService[F], b: SafelyDoSomethingService[F]) {
    def combineLogs =
      for {
        _ <- b.logs.flush { c =>
          a.logs.log(c)
        }
      } yield a.logs
  }

  "" should {
    "" in {

      def createResource[F[_] : Applicative: Sync] = {
        for {
          implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(MonadLog.createMonadLog[F, String])
          doSome = new SafelyDoSomethingService[F](1)
          doSome2 = new SafelyDoSomethingService[F](2)
          combo = new AggregateDoSomething[F](doSome, doSome2)
        } yield (doSome, doSome2, combo)
      }

      val program = createResource[IO].use {
        case (doSome, doSome2, combo) =>
          doSome.doIt.unsafeRunSync()
          doSome2.doIt.unsafeRunSync()

          Sync[IO].delay {
            val logsCombined = combo.combineLogs.unsafeRunSync()

            for {
              _ <- logsCombined.flush{ c =>
                println(c)
                IO.unit
              }
            } yield ()
          }.unsafeRunSync()

          IO.unit
      }

      program.unsafeRunSync()

      1 mustEqual 1
    }
  }
}
