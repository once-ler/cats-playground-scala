package com.eztier.testxmlfs2
package patients.domain

import java.time.LocalDateTime

import cats.{Functor}

case class Patient
(
  AdministrativeSex: Option[String],
  DateTimeofBirth: Option[String],
  EthnicGroup: Option[String],
  PatientAddress: Option[String],
  PatientName: Option[String],
  PhoneNumberHome: Option[String],
  Race: Option[String],
  Mrn: Option[String],
  dateCreated: Option[Long],
  dateLocal: Option[String]
)

trait PatientRepositoryAlgebra[F[_]] {
  def list(): F[List[Patient]]
  def insertMany(a: List[Patient]): F[Int]
}

class PatientService[F[_]: Functor](
  repository: PatientRepositoryAlgebra[F]
) {
  def list(): F[List[Patient]] =
    repository.list()

  def insertMany(a: List[Patient]): F[Int] =
    repository.insertMany(a)
}

object PatientService {
  def apply[F[_]: Functor](
    repository: PatientRepositoryAlgebra[F]
  ): PatientService[F] =
    new PatientService[F](repository)
}
