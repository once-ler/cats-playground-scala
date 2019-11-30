package com.eztier
package epmock.domain

import cats.implicits._
import cats.{Applicative, Functor, Monad, SemigroupK, Semigroupal, Show}
import cats.effect.{Async, Concurrent, Sync}
import fs2.Pipe
import fs2.Stream

import clickmock.domain.{Ck_ParticipantService, Ck_Participant, Ck_Participant_CustomAttributesManager}
import common._
import common.Util._

class PatientAggregator[F[_]: Applicative: Async: Concurrent](patientService: EpPatientService[F], participantService: Ck_ParticipantService[F]) {
  val fetchXmlPatients = (in: Stream[F, Int]) => Stream.eval(patientService.fetchPatients.compile.toList)

  def persistRuntimePatients: Pipe[F, List[EpPatient], List[Int]] = _.evalMap {
    in => Stream.eval(patientService.insertMany(in)).compile.toList
  }

  def fetchRuntimePatients: Pipe[F, List[Int], List[EpPatient]] = _.evalMap {
    in => Stream.eval(patientService.list()).flatMap(Stream.emits).compile.toList
  }

  def fetchParticipants: Pipe[F, List[EpPatient], (List[EpPatient], List[(Ck_Participant, Ck_Participant_CustomAttributesManager)])] = _.evalMap {
    in =>
      val fa = Applicative[F].pure(in)
      val fb = Stream.eval(participantService.listById(in.map(_.Mrn))).flatMap(Stream.emits).compile.toList

      // Or use Semigroupal.tuple2(fa, fb)
      Semigroupal[F].product(fa, fb)
  }

  def toLatestPatientPipeS: Pipe[F, List[EpPatient], List[EpPatient]] = _.evalMap {
    in =>
      val fb = in.filter(a => a.Mrn.isDefined)
        .groupBy(_.Mrn.get)
        .map(d => d._2.sortBy(b => - b.dateCreated.get).head)
        .toList

      fb.pure[F]
  }

  def removeNonExistentParticipants: Pipe[F, (List[EpPatient], List[(Ck_Participant, Ck_Participant_CustomAttributesManager)]), List[EpPatient]] = _.evalMap {
    in =>
      import cats.instances.string._    // for Eq
      import cats.instances.option._ // for Eq
      import cats.syntax.eq._ // for ===
      import cats.syntax.applicative._ // for pure

      val cm = in._2.map(_._2)
      val f = in._1.filter(a => cm.exists(_.medicalRecordNumber === a.Mrn))
      f.pure[F]
  }

  def parsePatient: Pipe[F, EpPatient, EpPatientTyped] = _.map {
    in =>

      implicit val csvconf = CSVConfig(delimiter = '^')

      val mrn = csvToCC(CSVConverter[List[Mrn]], in.Mrn, Mrn())
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

      val mrn1 = SemigroupK[Option].combineK(mrn.mrn1, mrn.mrn2)

      EpPatientTyped(
        mrn = mrn, patientName = pn, dob = in.DateTimeofBirth, race = rc, ethnicity = et, gender = in.AdministrativeSex, patientAddress = pa, phoneNumberHome = pnh, dateCreated = in.dateCreated, dateLocal = in.dateLocal
      )
  }

  def runWithH2 = {
    implicit val showPatient: Show[EpPatient] = a => s"${a.PatientName} ${a.dateCreated.toString}"

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

  def run =
    Stream.eval(patientService.fetchPatients.compile.toList)
      .through(toLatestPatientPipeS)
      .through(fetchParticipants)
      .through(removeNonExistentParticipants)
      .flatMap(Stream.emits)
      .through(parsePatient)
}

object PatientAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](patientService: EpPatientService[F], participantService: Ck_ParticipantService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, participantService)
}