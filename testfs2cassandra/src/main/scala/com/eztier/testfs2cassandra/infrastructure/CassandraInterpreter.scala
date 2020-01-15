package com.eztier
package testfs2cassandra.infrastructure

import cats.implicits._
import cats.effect.{Async, Concurrent, Resource}
import fs2._
import Stream._
import com.datastax.driver.core.{SimpleStatement, Statement}
import datasource.infrastructure.CassandraClient
import testfs2cassandra.domain._

class CassandraInterpreter[F[_]: Async: Concurrent](client: CassandraClient[F]) {
  def runTest = {

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
