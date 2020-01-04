package com.eztier.testdoobiefs2
package domain

import java.time.Instant
import cats.Functor
import fs2.Stream

case class Patient
(
  mrn: Option[String] = None,
  firstName: Option[String] = None,
  middleName: Option[String] = None,
  lastName: Option[String] = None,
  fullName: Option[String] = None,
  dateOfBirth: Option[Instant] = None,
  gender: Option[String] = None,
  ethnicity: Option[String] = None,
  race: Option[String] = None,
  street: Option[String] = None,
  stateProvince: Option[String] = None,
  postalCode: Option[String] = None,
  phoneNumber: Option[String] = None,
  email: Option[String] = None,
  suggest: Option[String] = None
)

trait PatientRepositoryAlgebra[F[_]] {
  def fetchPatients: Stream[F, Patient]
  def insertManyPatients(recs: List[Patient]): F[Int]
}

class PatientService[F[_] : Functor](repository: PatientRepositoryAlgebra[F]) {
  def fetchPatients =
    repository.fetchPatients

  def insertManyPatients(recs: List[Patient]) =
    repository.insertManyPatients(recs)
}

object PatientService {
  def apply[F[_] : Functor](respository: PatientRepositoryAlgebra[F]): PatientService[F] =
    new PatientService[F](respository)
}
