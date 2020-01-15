package com.eztier.datasource
package infrastructure

import cats.effect.{Async, Resource, Sync}
import fs2.Chunk

import com.datastax.driver.core.{BatchStatement, Cluster, ResultSet, ResultSetFuture, Session, Statement}
import com.datastax.driver.core.policies.ConstantReconnectionPolicy
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._
import java.net.InetSocketAddress
import java.util.concurrent.Executors

trait WithBlockingThreadPool {
  def blockingThreadPool[F[_]: Sync]: Resource[F, ExecutionContext] =
    Resource(Sync[F].delay {
      val executor = Executors.newFixedThreadPool(4)
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, Sync[F].delay(executor.shutdown()))
    })

  implicit def resultSetFutureToScala(f: ResultSetFuture): Future[ResultSet] = {
    val p = Promise[ResultSet]()
    Futures.addCallback(f,
      new FutureCallback[ResultSet] {
        def onSuccess(r: ResultSet) = p success r
        def onFailure(t: Throwable) = p failure t
      })
    p.future
  }

  implicit def listenableFutureToFuture[T](f: ListenableFuture[T]): Future[T] = {
    val p = Promise[T]()
    Futures.addCallback(f, new FutureCallback[T] {
      def onFailure(error: Throwable): Unit = p.failure(error)
      def onSuccess(result: T): Unit = p.success(result)
    })
    p.future
  }
}

class CassandraSession[F[_]: Async : Sync](endpoints: String, port: Int, user: Option[String] = None, pass: Option[String] = None)
  extends WithBlockingThreadPool {

  private val contactPoints = endpoints.split(',').map {
    a =>
      if (a.contains(":")) {
        val addr = a.split(":")
        new InetSocketAddress(addr(0), addr(1).toInt).getAddress
      } else new InetSocketAddress(a, port).getAddress
  }.toSeq.asJavaCollection

  private val cluster = {
    val clusterBuilder = Cluster.builder
      .addContactPoints(contactPoints)
      .withPort(port)
      .withReconnectionPolicy(new ConstantReconnectionPolicy(5000))

    user match {
      case Some(u) if pass.isDefined =>
        clusterBuilder.withCredentials(u, pass.get)
        ()
      case _ => ()
    }

    clusterBuilder.build()
  }

  def getSession: Resource[F, Session] =
    Resource.liftF {
      blockingThreadPool.use { ec: ExecutionContext =>
        implicit val cs = ec

        Async[F].async {
          (cb: Either[Throwable, Session] => Unit) =>

            val f: Future[Session] = cluster.connectAsync()

            f.onComplete {
              case Success(s) => cb(Right(s))
              case Failure(e) => cb(Left(e))
            }
        }
      }
    }
}

object CassandraSession {
  def apply[F[_]: Async : Sync](endpoints: String, port: Int, user: Option[String] = None, pass: Option[String] = None) =
    new CassandraSession[F](endpoints, port, user, pass)
}

class CassandraClient[F[_] : Async : Sync](session: Resource[F, Session])
  extends WithBlockingThreadPool {

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

  private def zipKV(
    in: AnyRef,
    filterFunc: java.lang.reflect.Field => Boolean = (_) => true,
    formatFunc: Any => Any = a => a
   ): (Array[String], Array[AnyRef]) =
      ((Array[String](), Array[AnyRef]()) /: in.getClass.getDeclaredFields.filter(filterFunc)) {
        (a, f) =>
          f.setAccessible(true)
          val k = a._1 :+ formatFunc(f.getName).asInstanceOf[String]

          val v = a._2 :+
            (formatFunc(f.get(in)) match {
              case Some(o) =>
                o match {
                  case a: Map[_, _] => a.asJava
                  case a: List[_] => a.asJava
                  case a: Vector[_] => a.asJava
                  case a: Seq[_] => a.asJava
                  case _ => o
                }
              case _ => null
            }).asInstanceOf[AnyRef]

          (k, v)
      }

  def insertManyAsync[A <: AnyRef](records: Chunk[A], keySpace: String = "", tableName: String = "") =
    session.use { s =>

      blockingThreadPool.use { ec: ExecutionContext =>
        implicit val cs = ec

        Async[F].async {
          (cb: Either[Throwable, ResultSet] => Unit) =>

            val batch = records.map {
              c =>
                val (keys, values) = zipKV(c)

                QueryBuilder.insertInto(keySpace, tableName).values(keys, values)
            }.toVector

            val batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED)
              .addAll(batch.asJava)

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
