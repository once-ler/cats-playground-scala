package com.eztier.epmock
package infrastructure.doobie

import cats.implicits._ // For Foldable[A] in updateMany
import doobie._
import doobie.implicits._
import cats.effect.Bracket
import doobie.{Query0, Transactor}

import domain._

private object PatientSQL {

  // Grab the latest.
  def listSql: Query0[EpPatient] = sql"""
    SELECT administrative_sex, date_timeof_birth, ethnic_group, patient_address, patient_name, phone_number_home, race, a.mrn, date_created, date_local
    FROM patient a join (select mrn, max(date_created) max_date_created from patient group by mrn) b on b.mrn = a.mrn and b.max_date_created = a.date_created
  """.query

  def insertManySql(a: List[EpPatient]): ConnectionIO[Int] = {
    val stmt = """
      insert into patient (administrative_sex, date_timeof_birth, ethnic_group, patient_address, patient_name, phone_number_home, race, mrn, date_created, date_local)
      values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    Update[EpPatient](stmt)
      .updateMany(a)
  }

  def truncateSql: Update0 = sql"""
    truncate table patient
  """.update

}

class DoobiePatientRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends EpPatientRepositoryAlgebra[F] {
  import PatientSQL._

  override def insertMany(a: List[EpPatient]): F[Int] = insertManySql(a).transact(xa)

  override def list(): F[List[EpPatient]] = listSql.to[List].transact(xa)

  override def truncate(): F[Int] = truncateSql.run.transact(xa)

  override def fetchPatients(): fs2.Stream[F, EpPatient] = ???
}

object DoobiePatientRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobiePatientRepositoryInterpreter[F] =
    new DoobiePatientRepositoryInterpreter(xa)
}
