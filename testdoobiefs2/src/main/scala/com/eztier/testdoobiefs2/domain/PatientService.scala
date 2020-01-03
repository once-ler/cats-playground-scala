package com.eztier.testdoobiefs2
package domain

import cats.Functor
import fs2.Stream

case class Patient
(
  mrn: Option[String],
  firstName: Option[String],
  lastName: Option[String]
)

trait PatientRepositoryAlgebra[F[_]] {
  def fetchPatients: Stream[F, Patient]
}

class PatientService[F[_] : Functor](respository: PatientRepositoryAlgebra[F]) {
  def fetchPatients =
    respository.fetchPatients
}

object PatientService {
  def apply[F[_] : Functor](respository: PatientRepositoryAlgebra[F]): PatientService[F] =
    new PatientService[F](respository)
}
