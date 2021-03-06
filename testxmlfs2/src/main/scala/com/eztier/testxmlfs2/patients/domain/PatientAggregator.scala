package com.eztier.testxmlfs2
package patients.domain

import cats.implicits._
import cats.{Applicative, Functor, Monad, SemigroupK, Semigroupal, Show}
import cats.effect.{Async, Concurrent, Sync}
import com.eztier.testxmlfs2.patients.infrastructure.file.XmlService
import fs2.Pipe
import fs2.Stream

import Util._

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

  def parsePatient: Pipe[F, Patient, Patient] = _.map {
    in =>

      implicit val csvconf = CSVConfig(delimiter = '^')

      val et = csvToCC(CSVConverter[List[Ethnicity]], in.EthnicGroup, Ethnicity())
      val pa = csvToCC(CSVConverter[List[PatientAddress]], in.PatientAddress, PatientAddress())
      val pn = csvToCC(CSVConverter[List[PatientName]], in.PatientName, PatientName())
      val rc = csvToCC(CSVConverter[List[Race]], in.Race, Race())
      val pnh = csvToCC(CSVConverter[List[PhoneNumberHome]], in.PhoneNumberHome, PhoneNumberHome())

      val et3 = SemigroupK[Option].combineK(
        et.ethnicity2,
        et.ethnicity1)

      // Order does not matter.
      val et4 = SemigroupK[Option].combineK(
        et.ethnicity1,
        et.ethnicity2)

      in
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
      .through(parsePatient)
      .showLinesStdOut

    // Stream.eval(Applicative[F].pure(()))
  }
}

object PatientAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](patientService: PatientService[F], participantService: ParticipantService[F], xmlService: XmlService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, participantService, xmlService)
}