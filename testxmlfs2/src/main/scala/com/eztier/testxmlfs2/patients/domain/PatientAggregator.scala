package com.eztier.testxmlfs2
package patients.domain

import cats.{Applicative, Show}
import cats.effect.{Async, Concurrent, Sync}
import com.eztier.testxmlfs2.patients.infrastructure.file.XmlService
import fs2.Pipe
import fs2.Stream

class PatientAggregator[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], xmlService: XmlService[F]) {
  def persistRuntimePatients: Pipe[F, List[Patient], List[Int]] = _.evalMap {
    in =>

      Stream.eval(patientService.insertMany(in)).compile.toList
  }

  def fetchRuntimePatients: Pipe[F, List[Int], List[Patient]] = _.evalMap {
    in =>

      Stream.eval(patientService.list()).flatMap(Stream.emits).compile.toList
  }

  def run = {
    implicit val showPatient: Show[Patient] = a => s"${a.PatientName}"

    // Stream.eval(patientService.truncate())
    
    val fa = xmlService.fetchPatients.compile.toList

    Stream.eval(fa)
      .through(persistRuntimePatients)
      .through(fetchRuntimePatients)
      .flatMap(Stream.emits)
      .showLinesStdOut

    // Stream.eval(Applicative[F].pure(()))
  }
}

object PatientAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], xmlService: XmlService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, xmlService)
}