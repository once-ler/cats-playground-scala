package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.EitherT
import cats.effect.{IO, Sync}
import cats.syntax._

import scala.concurrent.Future

class TrialAggregator[F[_]: Applicative : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) {

  // EitherT[F, A, B] == F[Either[A, B]]
  def run() = {


    trialContractService.get(1).map {
      a =>
        trialService.exists(a.trialId).map {
          b =>
            val fc: F[List[Junction]] = junctionService.list(b.trialArmSet)
            Functor[F].map(fc){
              c =>
                val fc1 = Applicative[F].pure(c)
            }
        }
    }

    // https://medium.com/@scalaisfun/optiont-and-eithert-in-scala-90241aba1bb7
    
  }
}

object TrialAggregator {
  def apply[F[_]: Functor : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService, junctionService)
}
