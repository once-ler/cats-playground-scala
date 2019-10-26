package src.com.eztier.testbadsqlmodel
package domain.trials

import cats.Applicative
import cats.data.{EitherT, OptionT}
import src.com.eztier.testbadsqlmodel.domain.TrialContractNotFoundError

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
  def exists(authorId: Option[Long]): EitherT[F, TrialContractNotFoundError.type, Unit] =
    authorId match {
      case Some(id) =>
        trialContractRepo.get(id)
          .toRight(TrialContractNotFoundError)
          .map(_ => ())
      case None =>
        EitherT.leftT(TrialContractNotFoundError)
    }
}

object TrialValidationInterpreter {
  def apply[F[_]: Applicative](trialContractRepo: TrialContractRepositoryAlgebra[F]): TrialContractValidationAlgebra[F] =
    new TrialContractValidationInterpreter[F](trialContractRepo)
}