package com.eztier.testdoobiefs2
package infrastructure

import cats.Applicative
import fs2.Stream
import cats.effect.Bracket
import cats.implicits._
import doobie._
import doobie.implicits._
import domain._

object DoobiePatientSql {
  def fetchPatientsSql2: Query0[Patient] = sql"""
    SELECT mrn, firstName, lastName FROM patient
  """.query

  def fetchPatientsSql: Query0[Patient] = sql"""SELECT id, name FROM gettingstarted""".query

  def insertManyPatientsSql =
    """
      INSERT INTO patient (
        mrn,
        firstName,
        middleName,
        lastName,
        fullName,
        dateOfBirth,
        gender,
        ethnicity,
        race,
        street,
        stateProvince,
        postalCode,
        phoneNumber,
        email,
        suggest
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """
}

class DoobiePatientInterpreter[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]) extends PatientRepositoryAlgebra[F] {

  import DoobiePatientSql._

  override def fetchPatients: Stream[F, Patient] =
    fetchPatientsSql.stream.transact(xa)

  override def insertManyPatients(recs: List[Patient]): F[Int] =
    Update[Patient](insertManyPatientsSql)
      .updateMany(recs)
      .transact(xa)
      .handleErrorWith{
        e =>
          /*
          for {
            _ <- logs.log(Chain.one(WrapThrowable(e).printStackTraceAsString))
          } yield -1
          */

          e.printStackTrace()
          Applicative[F].pure(-1)
      }

}

object DoobiePatientInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobiePatientInterpreter[F] =
    new DoobiePatientInterpreter[F](xa)
}
