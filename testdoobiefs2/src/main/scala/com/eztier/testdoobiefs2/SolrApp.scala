package com.eztier
package testdoobiefs2

import cats.implicits._
import cats.effect._
import cats.effect.{Async, ContextShift, IOApp, Resource}
import io.circe.config.{parser => ConfigParser}
import doobie.util.ExecutionContexts
import config._
import domain._
import infrastructure._

object SolrApp {
  def createSolrClientAggregator[F[_]: Async: ContextShift: ConcurrentEffect] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testdoobiefs2")) // Lifts an applicative into a resource.
      // _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.solr)) // Lifts an applicative into a resource. Resource[Tuple1, Nothing[Unit]]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.solr.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      // xa <- DatabaseConfig.dbTransactor(conf.solr, connEc, Blocker.liftExecutionContext(txnEc))
      xa <- DatabaseConfig.dbDriver(conf.solr, connEc, Blocker.liftExecutionContext(txnEc))
      patientRepo = DoobiePatientInterpreter[F](xa)
      patientService = PatientService(patientRepo)
    } yield patientService
  }
}
