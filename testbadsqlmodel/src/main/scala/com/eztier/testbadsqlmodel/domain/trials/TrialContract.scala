package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.{EitherT, OptionT}
import com.eztier.testbadsqlmodel.domain.TrialContractNotFoundError

case class TrialContract
(
  id: Long = -1,
  name: String = "",
  trialId: Option[Long] = None,
  fixedGeneralItemSet: Option[Long] = None,
  fixedPersonnelItemSet: Option[Long] = None,
  indirectRate: Option[Double] = None,
  costPerParticipant: Option[Double] = None
)

trait TrialContractRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, TrialContract]
}

trait TrialContractValidationAlgebra[F[_]] {
  def exists(id: Option[Long]): EitherT[F, String, Unit]
}

class TrialContractValidationInterpreter[F[_]: Applicative](trialContractRepo: TrialContractRepositoryAlgebra[F])
  extends TrialContractValidationAlgebra[F] {
  def exists(id: Option[Long]): EitherT[F, String, Unit] =
    id match {
      case Some(id) =>
        trialContractRepo.get(id)
          .toRight("Trial contract not found.")
          .map(_ => ())
      case None =>
        EitherT.leftT("Trial contract not found.")
    }
}

object TrialContractValidationInterpreter {
  def apply[F[_]: Applicative](trialContractRepo: TrialContractRepositoryAlgebra[F]): TrialContractValidationAlgebra[F] =
    new TrialContractValidationInterpreter[F](trialContractRepo)
}

class TrialContractService[F[_]](
  repository: TrialContractRepositoryAlgebra[F],
  validation: TrialContractValidationAlgebra[F]
) {
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, String, TrialContract] =
    repository.get(id).toRight("Trial contract not found.")
}

object TrialContractService {
  def apply[F[_]](
  repository: TrialContractRepositoryAlgebra[F],
  validation: TrialContractValidationAlgebra[F]
): TrialContractService[F] =
  new TrialContractService[F](repository, validation)
}