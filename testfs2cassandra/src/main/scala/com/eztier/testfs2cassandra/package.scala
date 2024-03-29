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

  def initializeDocumentAggregatorResource[F[_]: Async : Applicative: ContextShift: ConcurrentEffect : Timer] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      // _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.db.eventstore))
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.eventstore.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf.db.eventstore, connEc, Blocker.liftExecutionContext(txnEc))
      documentMetadataRepo = new DoobieDocumentMetadataInterpreter[F](xa, conf.db.eventstore)
      documentMetadataService = new DocumentMetadataService[F](documentMetadataRepo)
      documentRepo = new DoobieDocumentInterpreter[F](xa, conf.db.eventstore)
      documentService = new DocumentService[F](documentRepo)
      documentXmlRepo = new DocumentHttpInterpreter[F](conf.http.entity)
      documentXmlService = new DocumentXmlService(documentXmlRepo)
      s <- Resource.liftF(Semaphore[F](conf.textExtractor.concurrency))
      documentExtractRepo = new TextExtractInterpreter[F](conf.textExtractor.concurrency, s, Some(conf.textExtractor.pathPrefix)).initialize // Don't forget to initialize the worker pool.
      documentExtractService = new DocumentExtractService[F](documentExtractRepo)
      cs = CassandraSession[F](conf.cassandra.connection.host, conf.cassandra.connection.port, conf.cassandra.connection.user, conf.cassandra.connection.password).getSession
      cl = CassandraClient(cs)
      documentExtractPersistRepo = CassandraInterpreter(cl)
      documentExtractPersistService = new DocumentExtractPersistService[F](documentExtractPersistRepo)
      documentAggregator = new DocumentAggregator[F](documentMetadataService, documentService, documentXmlService, documentExtractService, documentExtractPersistService)
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

  def initializeTextExtractorResource[F[_]: Async : Applicative : ConcurrentEffect: ContextShift] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      s <- Resource.liftF(Semaphore[F](conf.textExtractor.concurrency))
      tx = new TextExtractInterpreter[F](conf.textExtractor.concurrency, s)
    } yield tx
  }
}
