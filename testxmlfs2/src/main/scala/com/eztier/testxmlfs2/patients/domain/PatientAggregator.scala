package com.eztier.testxmlfs2
package patients.domain

import cats.{Applicative, Functor, Show}
import cats.effect.{Async, Concurrent, Sync}
import com.eztier.testxmlfs2.patients.infrastructure.file.XmlService
import fs2.Pipe
import fs2.Stream

class PatientAggregator[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], participantService: ParticipantService[F], xmlService: XmlService[F]) {
  val fetchXmlPatients = (in: Stream[F, Int]) => Stream.eval(xmlService.fetchPatients.compile.toList)

  def persistRuntimePatients: Pipe[F, List[Patient], List[Int]] = _.evalMap {
    in => Stream.eval(patientService.insertMany(in)).compile.toList
  }

  def fetchRuntimePatients: Pipe[F, List[Int], List[Patient]] = _.evalMap {
    in => Stream.eval(patientService.list()).flatMap(Stream.emits).compile.toList
  }

  def fetchParticipants: Pipe[F, List[Patient], (List[Patient], List[Participant])] = _.evalMap {
    in =>
      val a = Stream.eval(participantService.list(in)).flatMap(Stream.emits).compile.toList

      val fb = Functor[F].map(a) {
        out =>
          (in, out)
      }

      fb
  }

  def run = {
    implicit val showPatient: Show[Patient] = a => s"${a.PatientName} ${a.dateCreated.toString}"

    Stream.eval(patientService.truncate())
      .through(fetchXmlPatients)
      .through(persistRuntimePatients)
      .through(fetchRuntimePatients)
      .flatMap(Stream.emits)
      .showLinesStdOut

    // Stream.eval(Applicative[F].pure(()))
  }
}

object PatientAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], participantService: ParticipantService[F], xmlService: XmlService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, participantService, xmlService)
}