package com.eztier.testbadsqlmodel
package domain.trials

import cats.Functor
import cats.data.EitherT
import cats.effect.Sync
import cats._

class TrialAggregator[F[_]: Functor : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F]) {

  // EitherT[F, A, B] == F[Either[A, B]]
  def run() = {

    /*
    trialContractService.get(1).map {
      a =>
        trialService.exists(a.trialId).map {
          b =>
            b.trialArmSet
        }
    }
    */

    for {
      a <- trialContractService.get(1)
      b <- trialService.exists(a.trialId)
      c <- Functor[Long].map(trialArmService.get)(b.trialArmSet)
    } yield ???

  }
}

object TrialAggregator {
  def apply[F[_]: Functor : Sync](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService)
}
