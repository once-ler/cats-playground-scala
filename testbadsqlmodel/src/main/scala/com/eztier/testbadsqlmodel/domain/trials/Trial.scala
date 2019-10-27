package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Functor}
import cats.data.{EitherT, OptionT}
import com.eztier.testbadsqlmodel.domain.TrialNotFoundError

case class Trial
(
  id: Long,
  name: String,
  trialArmSet: Option[Long]
)

trait TrialRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, Trial]
}

trait TrialValidationAlgebra[F[_]] {
  def exists(id: Option[Long]): EitherT[F, TrialNotFoundError.type, Unit]
}

class TrialValidationInterpreter[F[_]: Applicative](trialRepo: TrialRepositoryAlgebra[F])
  extends TrialValidationAlgebra[F] {
  def exists(id: Option[Long]): EitherT[F, TrialNotFoundError.type, Unit] =
    id match {
      case Some(id) =>
        trialRepo.get(id)
          .toRight(TrialNotFoundError) // Converts OptionT[F, A] to EitherT[F, L, A]
          .map(_ => ())
      case None =>
        // EitherT.leftT, EitherT.rightT is alias for EitherT.pure is same as EitherT.left[Unit](TrialNotFoundError.pure[F])
        EitherT.leftT(TrialNotFoundError)
    }
}

object TrialValidationInterpreter {
  def apply[F[_]: Applicative](trialRepo: TrialRepositoryAlgebra[F]): TrialValidationAlgebra[F] =
    new TrialValidationInterpreter[F](trialRepo)
}

class TrialService[F[_]](
  repository: TrialRepositoryAlgebra[F],
  validation: TrialValidationAlgebra[F]
) {
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, TrialNotFoundError.type, Trial] =
    repository.get(id).toRight(TrialNotFoundError)
}

object TrialService {
  def apply[F[_]](
    repository: TrialRepositoryAlgebra[F],
    validation: TrialValidationAlgebra[F]
  ): TrialService[F] =
    new TrialService[F](repository, validation)
}
