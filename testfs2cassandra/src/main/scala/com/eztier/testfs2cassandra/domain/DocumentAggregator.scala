package com.eztier
package testfs2cassandra.domain

import cats.implicits._
import cats.Functor
import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import fs2.concurrent.Queue
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.duration._

class DocumentAggregator[F[_]: Functor :Timer :Concurrent](documentMetadataService: DocumentMetadataService[F], documentService: DocumentService[F], documentXmlService: DocumentXmlService[F]) {

  private def toPersistDb: Pipe[F, Chunk[Document], Int] = _.evalMap { c =>
    documentService.insertMany(c)
  }

  // def getDocumentXml: Stream[F, Int] = {
  def getDocumentXml = {

    val src = documentMetadataService.list()

    /*
    documentXmlService
      .fetchDocumentXml(src)
      .groupWithin(10, 60.seconds)
      // .chunkN(10)
      .through(toPersistDb)
    */

    for {
      queue <- Stream.eval(Queue.unbounded[F, (String, String)])
      s <- Stream(
        src.evalMap(t => queue.enqueue1(t)).drain,
        queue.dequeue.groupWithin(10, 10.seconds).flatMap {c =>
          documentXmlService.fetchChunkDocumentXml(c).through(toPersistDb).showLinesStdOut
        }.drain
      ).parJoinUnbounded
    } yield s

  }

}
