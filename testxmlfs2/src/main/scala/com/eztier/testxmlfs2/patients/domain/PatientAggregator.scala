package com.eztier.testxmlfs2
package patients.domain

import cats.{Applicative, Functor, Semigroupal, Show}
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
      val fa = Applicative[F].pure(in)
      val fb = Stream.eval(participantService.list(in)).flatMap(Stream.emits).compile.toList

      // Or use Semigroupal.tuple2(fa, fb)
      Semigroupal[F].product(fa, fb)
  }

  def removeNonExistentParticipants: Pipe[F, (List[Patient], List[Participant]), List[Patient]] = _.evalMap {
    in =>
      import cats.instances.string._    // for Eq
      import cats.instances.option._ // for Eq
      import cats.syntax.eq._ // for ===
      import cats.syntax.applicative._ // for pure

      val f = in._1.filter(a => in._2.exists(_.medicalRecordNumber === a.Mrn))
      f.pure[F]
  }

  def run = {
    implicit val showPatient: Show[Patient] = a => s"${a.PatientName} ${a.dateCreated.toString}"

    Stream.eval(patientService.truncate())
      .through(fetchXmlPatients)
      .through(persistRuntimePatients)
      .through(fetchRuntimePatients)
      .through(fetchParticipants)
      .through(removeNonExistentParticipants)
      .flatMap(Stream.emits)
      .showLinesStdOut

    // Stream.eval(Applicative[F].pure(()))
  }
}

object PatientAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], participantService: ParticipantService[F], xmlService: XmlService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, participantService, xmlService)
}