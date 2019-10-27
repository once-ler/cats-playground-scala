package com.eztier.testbadsqlmodel
package domain.trials

class TrialAggregator[F[_]](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F]) {

}

object TrialAggregator {
  def apply[F[_]](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService)
}
