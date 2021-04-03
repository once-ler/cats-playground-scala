package com.eztier.testfs2cassandra
package infrastructure

import cats.effect.Bracket
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import fs2.{Chunk, Stream}

import config._
import domain._

private object DocumentMetadataSQL {
  // implicit val optionListStringMeta: Meta[Option[List[String]]] =

  def listSql(schema: String): Query0[(String, String)] = sql"""
    select doc_id, doc_other_id from $schema.document_metadata
  """.query

  def listAllSql(schema: String): Query0[DocumentMetadata] = sql"""
    select id,domain,root_type,root_id,root_owner,root_associates,root_company,root_status,root_display,
    root_display_long,doc_id,doc_other_id,doc_file_path,doc_object_path,doc_category,doc_name,
    doc_date_created,doc_year_created from $schema.document_metadata
  """.query[DocumentMetadata]

  def listPartialSql(schema: String): Query0[DocumentPartial] = sql"""
    select domain, root_type, root_id, doc_id, doc_name, doc_date_created,doc_year_created from $schema.document_metadata
  """.query
}

class DoobieDocumentMetataInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F], val conf: DatabaseConfig) extends DocumentMetadataRepo[F] {
  import DocumentMetadataSQL._
  val schema = conf.schema.getOrElse("irb") 

  override def list(): Stream[F, (String, String)] = listSql(schema).stream.flatMap { a =>
    println(a._1)
    Stream.emit(a)
  }.transact(xa)

  override def listAll(): Stream[F, DocumentMetadata] = listAllSql(schema).stream.flatMap { a =>
    println(a.id)
    Stream.emit(a)
  }.transact(xa)

  override def listPartial(): Stream[F, DocumentPartial] = listPartialSql(schema).stream.flatMap { a =>
    println(a.doc_id)
    Stream.emit(a)
  }.transact(xa)
}

private object DocumentSQL {
  def insertManySql(a: Chunk[Document], schema: String): ConnectionIO[Int] = {
    val stmt =
      s"""
        insert into $schema.document (doc_id, doc_other_id, doc_xml)
        values (?, ?, ?::xml)
        on conflict (doc_id) do update
        set doc_other_id = excluded.doc_other_id, doc_xml = excluded.doc_xml
      """

    Update[Document](stmt)
      .updateMany(a)
  }
}

class DoobieDocumentInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F], conf: DatabaseConfig) extends DocumentRepo[F] {
  import DocumentSQL._
  val schema = conf.schema.getOrElse("irb") 
  override def insertMany(a: Chunk[Document]): F[Int] = insertManySql(a, schema).transact(xa)
}

