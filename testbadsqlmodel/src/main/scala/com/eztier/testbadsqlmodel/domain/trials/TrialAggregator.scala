package com.eztier.testbadsqlmodel
package domain.trials

import java.util.concurrent.Executors

import cats.{Applicative, Functor}
import cats.data.EitherT
import cats.effect.{IO, Sync}
import fs2.Stream

import scala.concurrent.ExecutionContext
// import cats.syntax._
import cats.implicits._
import cats.effect._

import scala.concurrent.Future

import domain.ValidationError

class TrialAggregator[F[_]: Applicative : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) {

  // EitherT[F, A, B] == F[Either[A, B]]
  def run[F: Concurrent[?[_]]]()  = {
    // https://medium.com/@scalaisfun/optiont-and-eithert-in-scala-90241aba1bb7

    val action = for {
      a <- trialContractService.get(1)
      b <- trialService.exists(a.trialId)
      c <- EitherT.rightT(b.trialArmSet)
      d <- EitherT.liftF(junctionService.list(c))
    } yield d

    val maybeItems = action.value.map {
      case Right(list) => list
      case Left(_) => List[Junction]()
    }

    implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
    implicit val concurrent = IO.ioConcurrentEffect(IO.contextShift(ec))
    val concurrency = 4
    // def eval[F[_],A](f: F[A]): Stream[F,A]
    val computations = Stream.eval(maybeItems).map(
      _.map {
        b =>
          trialArmService.get(b.itemId).value
      }
    ).parJoin(concurrency)

    computations.covary[F].showLinesStdOut
  }
}

object TrialAggregator {
  def apply[F[_]: Functor : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService, junctionService)
}
