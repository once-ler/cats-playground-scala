package com.eztier.testxmlfs2
package patients.infrastructure.doobie

import cats.data.NonEmptyList
import doobie._
import doobie.implicits._
import cats.effect.Bracket
import doobie.{Query0, Transactor}
import patients.domain._

private object ParticipantSQL {
  def listSql(a: List[Patient]): Query0[Participant] = {
    val b = a.filter(!_.Mrn.isEmpty).map(_.Mrn.get)

    (fr"""
    SELECT medicalRecordNumber
    FROM participant where """ ++ Fragments.in(fr"medicalRecordNumber", NonEmptyList.fromListUnsafe(b))
      ).query
  }
}

class DoobieParticipantRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends ParticipantRepositoryAlgebra[F] {
  import ParticipantSQL._

  override def list(a: List[Patient]): F[List[Participant]] = listSql(a).to[List].transact(xa)
}

object DoobieParticipantRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieParticipantRepositoryInterpreter[F] =
    new DoobieParticipantRepositoryInterpreter(xa)
}