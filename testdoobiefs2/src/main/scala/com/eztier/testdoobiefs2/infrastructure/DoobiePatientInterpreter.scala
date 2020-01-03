package com.eztier.testdoobiefs2
package infrastructure

import fs2.Stream
import cats.effect.Bracket
import doobie._
import doobie.implicits._

import domain._

object DoobiePatientSql {
  def fetchPatientsSql: Query0[Patient] = sql"""
    SELECT mrn, firstName, lastName
    FROM patient
  """.query

}

class DoobiePatientInterpreter[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]) extends PatientRepositoryAlgebra[F] {

  import DoobiePatientSql._

  override def fetchPatients: Stream[F, Patient] =
    fetchPatientsSql.stream.transact(xa)

}

object DoobiePatientInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobiePatientInterpreter[F] =
    new DoobiePatientInterpreter[F](xa)
}
