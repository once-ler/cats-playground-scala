package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.{EitherT, OptionT}
import com.eztier.testbadsqlmodel.domain.TrialArmNotFoundError

case class TrialArm
(
  id: Long,
  name: String,
  numParticipants: Option[Int],
  variableProcedureItemSet: Option[Long],
  variableGeneralItemSet: Option[Long]
)

trait TrialArmRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, TrialArm]
}

trait TrialArmValidationAlgebra[F[_]] {
  def exists(id: Option[Long]): EitherT[F, TrialArmNotFoundError.type, Unit]
}

class TrialArmValidationInterpreter[F[_]: Applicative](trialArmRepo: TrialArmRepositoryAlgebra[F])
  extends TrialArmValidationAlgebra[F] {
  def exists(id: Option[Long]): EitherT[F, TrialArmNotFoundError.type, Unit] =
    id match {
      case Some(id) =>
        trialArmRepo.get(id)
          .toRight(TrialArmNotFoundError)
          .map(_ => ())
      case None =>
        EitherT.leftT(TrialArmNotFoundError)
    }
}

object TrialArmValidationInterpreter {
  def apply[F[_]: Applicative](trialArmRepo: TrialArmRepositoryAlgebra[F]): TrialArmValidationAlgebra[F] =
    new TrialArmValidationInterpreter[F](trialArmRepo)
}

class TrialArmService[F[_]](
  repository: TrialArmRepositoryAlgebra[F],
  validation: TrialArmValidationAlgebra[F]
) {
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, TrialArmNotFoundError.type, TrialArm] =
    repository.get(id).toRight(TrialArmNotFoundError)
}

object TrialArmService {
  def apply[F[_]](
    repository: TrialArmRepositoryAlgebra[F],
    validation: TrialArmValidationAlgebra[F]
  ): TrialArmService[F] =
  new TrialArmService[F](repository, validation)
}
