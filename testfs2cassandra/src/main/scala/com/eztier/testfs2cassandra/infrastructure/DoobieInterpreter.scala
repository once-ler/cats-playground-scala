package com.eztier.testfs2cassandra
package infrastructure

import cats.effect.Bracket
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import domain._
import doobie.util.query.Query0
import fs2.Stream

private object DocumentMetadataSQL {
  def listSql: Query0[String] = sql"""
    select doc_other_id from irb.document_metadata
  """.query
}

class DoobieDocumentMetataInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])extends DocumentMetadataRepo[F] {
  import DocumentMetadataSQL._
  override def list(): Stream[F, String] = listSql.stream.transact(xa)
}

private object DocumentSQL {
  def insertManySql(a: List[Document]): ConnectionIO[Int] = {
    val stmt =
      """
        insert into irb.document (doc_other_id, doc_xml)
        values (?, ?)
      """
  }
}

class DoobieDocumentInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F]) extends DocumentRepo[F] {
  import DocumentSQL._
  override def insertMany(a: List[Document]): F[Int] = insertManySql(a).transact(xa)
}

