package src.com.eztier.testbadsqlmodel
package domain.trials

import cats.Applicative
import cats.data.{EitherT, OptionT}
import src.com.eztier.testbadsqlmodel.domain.TrialArmNotFoundError

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
  def exists(authorId: Option[Long]): EitherT[F, TrialArmNotFoundError.type, Unit] =
    authorId match {
      case Some(id) =>
        trialArmRepo.get(id)
          .toRight(TrialArmNotFoundError)
          .map(_ => ())
      case None =>
        EitherT.leftT(TrialArmNotFoundError)
    }
}

object TrialValidationInterpreter {
  def apply[F[_]: Applicative](trialArmRepo: TrialArmRepositoryAlgebra[F]): TrialArmValidationAlgebra[F] =
    new TrialArmValidationInterpreter[F](trialArmRepo)
}