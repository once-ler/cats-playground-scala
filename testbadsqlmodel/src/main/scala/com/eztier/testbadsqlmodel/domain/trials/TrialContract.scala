package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.{EitherT, OptionT}
import com.eztier.testbadsqlmodel.domain.TrialContractNotFoundError

case class TrialContract
(
  id: Long,
  name: String,
  trialId: Option[Long],
  fixedGeneralItemSet: Option[Long],
  fixedPersonnelItemSet: Option[Long],
  indirectRate: Option[Double],
  costPerParticipant: Option[Double]
)

trait TrialContractRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, TrialContract]
}

trait TrialContractValidationAlgebra[F[_]] {
  def exists(id: Option[Long]): EitherT[F, TrialContractNotFoundError.type, Unit]
}

class TrialContractValidationInterpreter[F[_]: Applicative](trialContractRepo: TrialContractRepositoryAlgebra[F])
  extends TrialContractValidationAlgebra[F] {
  def exists(id: Option[Long]): EitherT[F, TrialContractNotFoundError.type, Unit] =
    id match {
      case Some(id) =>
        trialContractRepo.get(id)
          .toRight(TrialContractNotFoundError)
          .map(_ => ())
      case None =>
        EitherT.leftT(TrialContractNotFoundError)
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
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, TrialContractNotFoundError.type, TrialContract] =
    repository.get(id).toRight(TrialContractNotFoundError)
}

object TrialContractService {
  def apply[F[_]](
  repository: TrialContractRepositoryAlgebra[F],
  validation: TrialContractValidationAlgebra[F]
): TrialContractService[F] =
  new TrialContractService[F](repository, validation)
}