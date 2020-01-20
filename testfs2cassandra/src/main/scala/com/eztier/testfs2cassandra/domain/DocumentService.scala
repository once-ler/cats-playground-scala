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
}

class DocumentXmlService[F[_]](repo: DocumentXmlRepo[F]) {
  def fetchDocumentXml(src: Stream[F, (String, String)]): Stream[F, Document] =
    repo.fetchDocumentXml((src))
}
