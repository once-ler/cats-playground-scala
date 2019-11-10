package com.eztier.testxmlfs2
package patients.domain

import cats.Functor

case class Participant
(
  medicalRecordNumber: Option[String]
)

trait ParticipantRepositoryAlgebra[F[_]] {
  def list(a: List[Patient]): F[List[Participant]]
}

class ParticipantService[F[_]: Functor](
  repository: ParticipantRepositoryAlgebra[F]
) {
  def list(a: List[Patient]): F[List[Participant]] =
    repository.list(a)
}

object ParticipantService {
  def apply[F[_]: Functor](
    repository: ParticipantRepositoryAlgebra[F]
  ): ParticipantService[F] =
    new ParticipantService[F](repository)
}