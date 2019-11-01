package com.eztier.testbadsqlmodel
package domain.trials

import cats.data.EitherT
import cats.{Applicative, Functor}

case class Junction
(
  setId: Long,
  itemId: Long
)

trait JunctionRepositoryAlgebra[F[_]] {
  def list(id: Long): F[List[Junction]]
  def list(id: Option[Long]): F[List[Junction]]
}

class JunctionService[F[_]: Applicative](
  repository: JunctionRepositoryAlgebra[F]
) {
  def list(id: Long): F[List[Junction]] = repository.list(id)

  def list(id: Option[Long]): EitherT[F, String, List[Junction]] =
    id match {
      case Some(a) => EitherT.liftF(repository.list(a))
      case None => EitherT.leftT("Set id not provided.")
    }
}

object JunctionService {
  def apply[F[_]: Applicative](
    repository: JunctionRepositoryAlgebra[F]
  ): JunctionService[F] = new JunctionService[F](repository)
}
