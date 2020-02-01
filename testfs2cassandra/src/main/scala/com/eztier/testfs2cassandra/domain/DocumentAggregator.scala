package com.eztier
package testfs2cassandra.domain

import cats.implicits._
import cats.Functor
import cats.effect.{Concurrent, Timer}
import fs2.concurrent.Queue
import fs2.{Chunk, Pipe, Stream}
import scala.concurrent.duration._

import common.Util._

class DocumentAggregator[F[_]: Functor :Timer :Concurrent](
  documentMetadataService: DocumentMetadataService[F],
  documentService: DocumentService[F],
  documentXmlService: DocumentXmlService[F],
  documentExtractService: DocumentExtractService[F],
  documentExtractPersistService: DocumentExtractPersistService[F]
) {

  private def toPersistDb: Pipe[F, Chunk[Document], Int] = _.evalMap { c =>
    documentService.insertMany(c)
  }

  private def getSourceToGetMetadata: Stream[F, (String, String)] = documentMetadataService.list()
  private def getSourceToExtract: Stream[F, DocumentMetadata] = documentMetadataService.listAll()

  def getDocumentXml = {

    val src = getSourceToGetMetadata

    for {
      queue <- Stream.eval(Queue.bounded[F, (String, String)](1))
      s <- Stream(
        src.evalMap(t => queue.enqueue1(t)).drain,
        queue.dequeue.groupWithin(10, 10.seconds).flatMap {c =>
          documentXmlService.fetchChunkDocumentXml(c).through(toPersistDb).showLinesStdOut
        }.drain
      ).parJoinUnbounded
    } yield s

  }

  def extractDocument = {
    val src = getSourceToExtract

    for {
      queue <- Stream.eval(Queue.bounded[F, DocumentMetadata](1))
      s <- Stream(
        src.evalMap(t => queue.enqueue1(t)).drain,
        queue.dequeue
          .groupWithin(5, 10.seconds)
          .flatMap(a => Stream.emits(a.toVector))
          .through(documentExtractService.extractDocument)
          .through(filterSome)
          .through(documentExtractPersistService.insertManyAsync(20) _)
          .drain
      ).parJoinUnbounded
    } yield s
  }

}
