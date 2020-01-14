package com.eztier.testfs2cassandra
package infrastructure

import cats.effect.{Async, Concurrent, Resource}
import shapeless.{HList, Witness}
import spinoco.fs2.cassandra.{CassandraSession, KeySpace, Table}
import fs2._
import Stream._
import domain._

class CassandraInterpreter[F[_]: Async: Concurrent](session: Resource[F, CassandraSession[F]]) {

  def createTest = {
    val ks = new KeySpace("dwh")
    val t1 = {
      ks.table[DocumentExtracted]
        .partition('domain)
        .partition('root_type)
        .partition('root_id)
        .cluster('doc_id)
        .build("ca_document_extracted")
    }

    Stream.resource(session).flatMap {
      cs =>
        Stream.eval(cs.create(t1))
    }
  }

  def insertMany = {

    val b0 = {

    }

  }

}

object CassandraInterpreter {
  def apply[F[_]: Async: Concurrent](session: Resource[F, CassandraSession[F]]): CassandraInterpreter[F] = new CassandraInterpreter[F](session)
}
