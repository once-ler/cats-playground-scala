package com.eztier.datasource
package infrastructure.cassandra

import cats.effect.{Async, Resource, Sync}
import fs2.Chunk

import com.datastax.driver.core.{ResultSet, Session, Statement}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CassandraClient[F[_] : Async : Sync](session: Resource[F, Session])
  extends WithBlockingThreadPool
  with WithInsertStatementBuilder
  with WithCreateStatementBuilder {

  def execAsync(ss: Statement): F[ResultSet] =
    session.use {
      s =>

        blockingThreadPool.use { ec: ExecutionContext =>
          implicit val cs = ec

          Async[F].async {
            (cb: Either[Throwable, ResultSet] => Unit) =>

              val f:Future[ResultSet] = s.executeAsync(ss)

              f.onComplete {
                case Success(s) => cb(Right(s))
                case Failure(e) => cb(Left(e))
              }
          }
        }
    }

  def insertManyAsync[A <: AnyRef](records: Chunk[A], keySpace: String = "", tableName: String = "") =
    session.use { s =>

      blockingThreadPool.use { ec: ExecutionContext =>
        implicit val cs = ec

        Async[F].async {
          (cb: Either[Throwable, ResultSet] => Unit) =>

            val batchStatement = buildInsertBatchStatement(records, keySpace, tableName)

            val f:Future[ResultSet] = s.executeAsync(batchStatement)

            f.onComplete {
              case Success(s) => cb(Right(s))
              case Failure(e) => cb(Left(e))
            }
        }
      }
    }

}

object CassandraClient {
  def apply[F[_] : Async](session: Resource[F, Session]): CassandraClient[F] = new CassandraClient[F](session)
}
