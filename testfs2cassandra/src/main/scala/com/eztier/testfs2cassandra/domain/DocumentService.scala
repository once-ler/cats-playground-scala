package com.eztier.testfs2cassandra
package domain

import fs2.{Chunk, Stream}

trait DocumentMetadataRepo[F[_]] {
  def list(): Stream[F, (String, String)]
  def listAll(): Stream[F, DocumentMetadata]
}

class DocumentMetadataService[F[_]](repo: DocumentMetadataRepo[F]) {
  def list(): Stream[F, (String, String)] =
    repo.list()

  def listAll(): Stream[F, DocumentMetadata] =
    repo.listAll()
}

trait DocumentRepo[F[_]] {
  def insertMany(a: Chunk[Document]): F[Int]
}

class DocumentService[F[_]](repo: DocumentRepo[F]) {
  def insertMany(a: Chunk[Document]): F[Int] =
    repo.insertMany(a)
}

trait DocumentXmlRepo[F[_]] {
  def fetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document]

  def fetchChunkDocumentXml(src: Chunk[(String, String)]): Stream[F, Chunk[Document]]
}

class DocumentXmlService[F[_]](repo: DocumentXmlRepo[F]) {
  def fetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document] =
    repo.fetchDocumentXml((src))

  def fetchChunkDocumentXml(src: Chunk[(String, String)]): Stream[F, Chunk[Document]] =
    repo.fetchChunkDocumentXml(src)
}

trait DocumentExtractRepo[F[_]] {
  def extractDocument(src: Stream[F, DocumentMetadata]): Stream[F, Option[DocumentExtracted]]
}

class DocumentExtractService[F[_]](repo: DocumentExtractRepo[F]) {
  def extractDocument(src: Stream[F, DocumentMetadata]): Stream[F, Option[DocumentExtracted]] =
    repo.extractDocument(src)
}

trait DocumentExtractPersistRepo[F[_]] {
  def insertManyAsync(batchSize: Int = 100)(src: Stream[F, DocumentExtracted]): Stream[F, Unit]
}

class DocumentExtractPersistService[F[_]](repo: DocumentExtractPersistRepo[F]) {
  def insertManyAsync(batchSize: Int = 100)(src: Stream[F, DocumentExtracted]): Stream[F, Unit] =
    repo.insertManyAsync(batchSize)(src)
}
