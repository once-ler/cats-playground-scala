package com.eztier.testbadsqlmodel
package domain.trials

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

  def list(id: Option[Long]): F[List[Junction]] =
    id match {
      case Some(a) => repository.list(a)
      case None => Applicative[F].pure(List[Junction]())
    }
}

object JunctionService {
  def apply[F[_]: Applicative](
    repository: JunctionRepositoryAlgebra[F]
  ): JunctionService[F] = new JunctionService[F](repository)
}
