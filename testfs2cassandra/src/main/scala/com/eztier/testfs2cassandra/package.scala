package com.eztier

import io.circe.config.{parser => ConfigParser}
import cats.Applicative
import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource, Timer}
import doobie.util.ExecutionContexts

package object testfs2cassandra {

  import config._
  import domain._
  import infrastructure._

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
}
