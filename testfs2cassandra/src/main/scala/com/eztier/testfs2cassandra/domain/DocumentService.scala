package com.eztier.testfs2cassandra
package domain

import fs2.{Chunk, Stream}

trait DocumentMetadataRepo[F[_]] {
  def list(): Stream[F, (String, String)]
}

class DocumentMetadataService[F[_]](repo: DocumentMetadataRepo[F]) {
  def list(): Stream[F, (String, String)] =
    repo.list()
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
  def extractDocument(src: Stream[F, DocumentMetadata]): Stream[F, Option[Extracted]]
}

class DocumentExtractService[F[_]](repo: DocumentExtractRepo[F]) {
  def extractDocument(src: Stream[F, DocumentMetadata]): Stream[F, Option[Extracted]] =
    repo.extractDocument(src)
}

trait DocumentExtractPersistRepo[F[_]] {
  def insertManyAsync(src: Stream[F, Extracted], batchSize: Int): Stream[F, Unit]
}

class DocumentExtractPersistService[F[_]](repo: DocumentExtractPersistRepo[F]) {
  def insertManyAsync(src: Stream[F, Extracted], batchSize: Int = 100): Stream[F, Unit] =
    repo.insertManyAsync(src, batchSize)
}
