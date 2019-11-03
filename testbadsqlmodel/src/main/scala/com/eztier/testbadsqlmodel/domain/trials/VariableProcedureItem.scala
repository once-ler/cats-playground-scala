package com.eztier.testbadsqlmodel
package domain.trials

import cats.Functor
import cats.data.{EitherT, OptionT}

case class VariableProcedureItem
(
  id: Long = -1,
  quantity: Int = 0,
  sponsorCost: Double = 0.00
)

trait VariableProcedureItemRepositoryAlgebra[F[_]] {
  def get(id: Long): OptionT[F, VariableProcedureItem]
}

class VariableProcedureItemService[F[_]](
  repository: VariableProcedureItemRepositoryAlgebra[F]
) {
def get(id: Long)(implicit F: Functor[F]): EitherT[F, String, VariableProcedureItem] =
  repository.get(id).toRight("Variable procedure item not found.")
}

object VariableProcedureItemService {
  def apply[F[_]](
    repository: VariableProcedureItemRepositoryAlgebra[F]
  ): VariableProcedureItemService[F] =
    new VariableProcedureItemService[F](repository)
}
