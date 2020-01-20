package com.eztier.testfs2cassandra
package domain

import fs2.{Chunk, Pipe, Stream}

class DocumentAggregator[F[_]](documentMetadataService: DocumentMetadataService[F], documentService: DocumentService[F], documentXmlService: DocumentXmlService[F]) {

  private def toPersistDb: Pipe[F, Chunk[Document], Int] = _.evalMap { c =>
    documentService.insertMany(c)
  }

  def getDocumentXml: Stream[F, Int] = {
    val src = documentMetadataService.list()

    documentXmlService
      .fetchDocumentXml(src)
      .chunkN(10)
      .through(toPersistDb)
  }

}
