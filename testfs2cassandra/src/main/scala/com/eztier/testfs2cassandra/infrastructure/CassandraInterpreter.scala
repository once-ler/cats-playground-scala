package com.eztier.testfs2cassandra
package infrastructure

import cats.implicits._
import cats.effect.{Async, Concurrent, Resource}
import fs2._
import Stream._
import com.datastax.driver.core.{Row, SimpleStatement, Statement}
import com.eztier.datasource.infrastructure.cassandra.CassandraClient
import domain._

class CassandraInterpreter[F[_]: Async: Concurrent](client: CassandraClient[F]) {
  def runCreateTest =
    Stream.eval{
      client.createAsync[DocumentExtracted]("dwh", Some("ca_document_extracted"))("domain", "root_type", "root_id")("doc_id")(Some("doc_id"), Some(1))
    }

  def runInsertTest: Stream[F, Row] = {

    val a = Extracted(
      domain = "test".some,
      root_type = "Foo".some,
      root_id = "1234".some,
      doc_id = "doc333".some,
      doc_file_path = "/some/path/file".some,
      content = "bizz buzz".some,
      metadata = Map("no" -> "way").some
    )

    val f = for {
      // a <- client.execAsync(new SimpleStatement("select count(*) from dwh.ca_document_extracted"))
      b <- client.insertManyAsync(Chunk.vector(Vector(a)), "dwh", "ca_document_extracted")
      s = b.one()
    } yield s

    Stream.eval(f)

  }
}

object CassandraInterpreter {
  def apply[F[_]: Async: Concurrent](client: CassandraClient[F]): CassandraInterpreter[F] = new CassandraInterpreter[F](client)
}
