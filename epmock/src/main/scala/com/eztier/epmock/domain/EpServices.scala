package com.eztier.epmock
package domain

import cats.Functor
import fs2.Stream

trait EpPatientRepositoryAlgebra[F[_]] {
  def list(): F[List[EpPatient]]
  def insertMany(a: List[EpPatient]): F[Int]
  def truncate(): F[Int]
  def fetchPatients(): Stream[F, EpPatient]
}

class EpPatientService[F[_]: Functor](repository: EpPatientRepositoryAlgebra[F]) {
  def list(): F[List[EpPatient]] = repository.list()

  def insertMany(a: List[EpPatient]): F[Int] =
    repository.insertMany(a)

  def truncate(): F[Int] =
    repository.truncate()

  def fetchPatients(): Stream[F, EpPatient] =
    repository.fetchPatients()
}

object EpPatientService {
  def apply[F[_]: Functor](repository: EpPatientRepositoryAlgebra[F]): EpPatientService[F] =
    new EpPatientService[F](repository)
}
