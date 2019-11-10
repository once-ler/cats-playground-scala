package com.eztier.testxmlfs2

import cats.implicits._
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import com.eztier.testxmlfs2.config.AppConfig
import io.circe.config.{parser => ConfigParser}
import doobie.util.ExecutionContexts
import patients.infrastructure.file.XmlService
import patients.domain._
import patients.infrastructure.doobie._
import patients.infrastructure.file._
import config._

/*
object App2 extends IOApp {
  import com.eztier.testxmlfs2.openstreetmap.infrastructure.OpenStreetMap
  import com.eztier.testxmlfs2.openstreetmap.infrastructure.Database
  val miner = Database.getMiner[IO]
  def run(args: List[String]): IO[ExitCode] = (OpenStreetMap[IO](miner)).run.as(ExitCode.Success)
}
*/

object App extends IOApp {
  def createPatientAggregator[F[_]: Async: ContextShift: ConcurrentEffect] =
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testxmlfs2")) // Lifts an applicative into a resource.
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.db)) // Lifts an applicative into a resource. Resource[Tuple1, Nothing[Unit]]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      patientRepo = DoobiePatientRepositoryInterpreter[F](xa)
      patientService = PatientService(patientRepo)
      participantRepo = DoobieParticipantRepositoryInterpreter[F](xa)
      participantService = ParticipantService(participantRepo)
      xmlService = new XmlService[F]
      patientAggregator = PatientAggregator[F](patientService, participantService, xmlService)
    } yield patientAggregator

  val agg = createPatientAggregator[IO].use(x => x.run.compile.drain)

  def run(args: List[String]): IO[ExitCode] = agg.as(ExitCode.Success)
}

/*
Result:
f
List(<DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Kips Bay, Manhattan Community Board 6, Manhattan, New York County, NYC, New York, 10016, USA</Neighborhood>
</DomainPlace>, <DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Midtown South, Manhattan Community Board 5, Manhattan, New York County, NYC, New York, 10016, United States of America</Neighborhood>
</DomainPlace>)

 */
