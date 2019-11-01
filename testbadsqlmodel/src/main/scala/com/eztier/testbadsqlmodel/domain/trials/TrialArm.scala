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
  def exists(id: Option[Long]): EitherT[F, String, TrialArm]
}

class TrialArmValidationInterpreter[F[_]: Applicative](trialArmRepo: TrialArmRepositoryAlgebra[F])
  extends TrialArmValidationAlgebra[F] {
  def exists(id: Option[Long]): EitherT[F, String, TrialArm] =
    id match {
      case Some(id) =>
        trialArmRepo.get(id)
          .toRight("Trial arm not found.")
      case None =>
        EitherT.leftT("Trial arm not found.")
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
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, String, TrialArm] =
    repository.get(id).toRight(s"Trial arm $id not found.")
}

object TrialArmService {
  def apply[F[_]](
    repository: TrialArmRepositoryAlgebra[F],
    validation: TrialArmValidationAlgebra[F]
  ): TrialArmService[F] =
  new TrialArmService[F](repository, validation)
}
