package com.eztier

import io.circe.config.{parser => ConfigParser}
import cats.Applicative
import cats.effect.concurrent.Semaphore
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource, Timer}
import doobie.util.ExecutionContexts

package object testfs2cassandra {

  import config._
  import domain._
  import infrastructure._

  import datasource.infrastructure.cassandra._

  def initializeDbResource[F[_]: Async : Applicative: ContextShift: ConcurrentEffect : Timer] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.db.eventstore))
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.eventstore.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf.db.eventstore, connEc, Blocker.liftExecutionContext(txnEc))
      documentMetadataRepo = new DoobieDocumentMetataInterpreter[F](xa)
      documentMetadataService = new DocumentMetadataService[F](documentMetadataRepo)
      documentRepo = new DoobieDocumentInterpreter[F](xa)
      documentService = new DocumentService[F](documentRepo)
      documentXmlRepo = new DocumentHttpInterpreter[F](conf.http.entity)
      documentXmlService = new DocumentXmlService(documentXmlRepo)
      documentAggregator = new DocumentAggregator[F](documentMetadataService, documentService, documentXmlService)
    } yield documentAggregator
  }

  def initializeCassandraResource[F[_]: Async :Applicative :ConcurrentEffect] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      cs = CassandraSession[F](conf.cassandra.connection.host, conf.cassandra.connection.port, conf.cassandra.connection.user, conf.cassandra.connection.password).getSession
      cl = CassandraClient(cs)
      ci = CassandraInterpreter(cl)
    } yield ci
  }

  def initializeTextExtractorResource[F[_]: Async : Applicative : ConcurrentEffect: ContextShift](ci: CassandraInterpreter[F]) = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      s <- Resource.liftF(Semaphore[F](conf.textExtractor.concurrency))
      tx = new TextExtractInterpreter[F](conf.textExtractor.concurrency, s, ci)
    } yield tx
  }
}
