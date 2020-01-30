package com.eztier.testfs2cassandra
package infrastructure

import cats.implicits._
import cats.effect.Bracket
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import domain._
import doobie.util.query.Query0
import doobie.util.update.Update
import fs2.{Chunk, Stream}

private object DocumentMetadataSQL {
  def listSql: Query0[(String, String)] = sql"""
    select doc_id, doc_other_id from irb.document_metadata
  """.query

  def listAllSql: Query0[DocumentMetadata] = sql"""
    select id,domain,root_type,root_id,root_owner,root_associates,root_company,root_status,root_display,
    root_display_long,doc_id,doc_other_id,doc_file_path,doc_object_path,doc_category,doc_name,
    doc_date_created,doc_year_created from irb.document_metadata
  """.query
}

class DoobieDocumentMetataInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])extends DocumentMetadataRepo[F] {
  import DocumentMetadataSQL._
  override def list(): Stream[F, (String, String)] = listSql.stream.flatMap { a =>
    println(a._1)
    Stream.emit(a)
  }.transact(xa)

  override def listAll(): Stream[F, DocumentMetadata] = listAllSql.stream.flatMap { a =>
    println(a.id)
    Stream.emit(a)
  }.transact(xa)
}

private object DocumentSQL {
  def insertManySql(a: Chunk[Document]): ConnectionIO[Int] = {
    val stmt =
      """
        insert into irb.document (doc_id, doc_other_id, doc_xml)
        values (?, ?, ?::xml)
      """

    Update[Document](stmt)
      .updateMany(a)
  }
}

class DoobieDocumentInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F]) extends DocumentRepo[F] {
  import DocumentSQL._
  override def insertMany(a: Chunk[Document]): F[Int] = insertManySql(a).transact(xa)
}
