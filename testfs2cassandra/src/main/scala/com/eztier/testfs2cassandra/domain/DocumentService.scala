package com.eztier.testfs2cassandra
package domain

import fs2.Stream

trait DocumentMetadataRepo[F[_]] {
  def list(): Stream[F, String]
}

class DocumentMetadataService[F[_]](repo: DocumentMetadataRepo[F]) {
  def list(): Stream[F, String] =
    repo.list()
}

trait DocumentRepo[F[_]] {
  def insertMany(a: List[Document]): F[Int]
}

class DocumentService[F[_]](repo: DocumentRepo[F]) {
  def insertMany(a: List[Document]): F[Int] =
    repo.insertMany(a)
}
