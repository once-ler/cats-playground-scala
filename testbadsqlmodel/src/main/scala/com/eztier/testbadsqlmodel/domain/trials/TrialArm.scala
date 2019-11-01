package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.{EitherT, OptionT}
import cats.effect.{Async, Concurrent, Sync}
import fs2.Stream

case class TrialArm
(
  id: Long = -1,
  name: String = "",
  numParticipants: Option[Int] = None,
  variableProcedureItemSet: Option[Long] = None,
  variableGeneralItemSet: Option[Long] = None
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
