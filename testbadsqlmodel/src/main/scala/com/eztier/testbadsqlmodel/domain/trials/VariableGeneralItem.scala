package com.eztier.testbadsqlmodel
package domain.trials

import cats.Functor
import cats.data.{EitherT, OptionT}

case class VariableGeneralItem
(
  id: Long = -1,
  quantity: Int = 0,
  sponsorCost: Double = 0.00
)

trait VariableGeneralItemRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, VariableGeneralItem]
}

class VariableGeneralItemService[F[_]](
    repository: VariableGeneralItemRepositoryAlgebra[F]
  ) {
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, String, VariableGeneralItem] =
    repository.get(id).toRight("Variable procedure item not found.")
}

object VariableGeneralItemService {
  def apply[F[_]](
    repository: VariableGeneralItemRepositoryAlgebra[F]
  ): VariableGeneralItemService[F] =
    new VariableGeneralItemService[F](repository)
}
