package com.eztier
package testfs2cassandra.domain

import cats.Functor
import cats.effect.{ConcurrentEffect, Timer}
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.duration._

class DocumentAggregator[F[_]: Functor :Timer](documentMetadataService: DocumentMetadataService[F], documentService: DocumentService[F], documentXmlService: DocumentXmlService[F]) {

  private def toPersistDb: Pipe[F, Chunk[Document], Int] = _.evalMap { c =>
    documentService.insertMany(c)
  }

  def getDocumentXml: Stream[F, Int] = {
    val src = Stream.awakeEvery[F](0.25.second) zipRight documentMetadataService.list()

    documentXmlService
      .fetchDocumentXml(src)
      .chunkN(10)
      .through(toPersistDb)
  }

}
