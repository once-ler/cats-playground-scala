package com.eztier.testxmlfs2
package patients.infrastructure.doobie

import cats.implicits._ // For Foldable[A] in updateMany
import doobie._
import doobie.implicits._
import cats.effect.Bracket
import doobie.{Query0, Transactor}
import patients.domain._

private object PatientSQL {

  def listSql: Query0[Patient] = sql"""
    SELECT administrative_sex, date_timeof_birth, ethnic_group, patient_address, patient_name, phone_number_home, race, mrn, date_created, date_local
    FROM patient
  """.query

  def insertManySql(a: List[Patient]): ConnectionIO[Int] = {
    val stmt = """
      insert into patient (administrative_sex, date_timeof_birth, ethnic_group, patient_address, patient_name, phone_number_home, race, mrn, date_created, date_local)
      values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    Update[Patient](stmt)
      .updateMany(a)
  }

  def truncateSql: Update0 = sql"""
    truncate table patient
  """.update

}

class DoobiePatientRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends PatientRepositoryAlgebra[F] {
  import PatientSQL._

  override def insertMany(a: List[Patient]): F[Int] = insertManySql(a).transact(xa)

  override def list(): F[List[Patient]] = listSql.to[List].transact(xa)

  override def truncate(): F[Int] = truncateSql.run.transact(xa)
}

object DoobiePatientRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobiePatientRepositoryInterpreter[F] =
    new DoobiePatientRepositoryInterpreter(xa)
}
