package com.eztier
package testhl7.tagless.multi
package Solr

import java.util.concurrent.Executors

import io.ino.solrs.future.ScalaFutureFactory.Implicit
import io.ino.solrs.CloudSolrServers.WarmupQueries

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{NodeSeq, XML}
import cats.implicits._
import cats.data.{Chain, EitherT}
import cats.{Applicative, Monad}
import cats.effect.{Async, Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import algae._
import algae.mtl.MonadLog
import io.chrisdavenport.log4cats.Logger
import common.CatsLogger._
import io.ino.solrs.{AsyncSolrClient, CloudSolrServers, RoundRobinLB}
import org.apache.solr.client.solrj.{SolrQuery, StreamingResponseCallback}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.params.SolrParams

object Package {
  import Domain._
  import Infrastructure._

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newFixedThreadPool(3)
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createCkAggregatorResource[F[_]: Async :ContextShift :ConcurrentEffect: Timer] = {
    for {
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(createMonadLog[F, Chain[String]])
      entityRepo = new SolrInterpreter[F]
      entityService = new SolrService(entityRepo)
    } yield entityService
  }
}

object Domain {
  trait SolrAlgebra[F[_]] {
    def search(query: Option[String]): F[NodeSeq]

    // def getEntityF(oid: Option[String], logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]]
    def insert(oid: Option[String]): F[Either[String, NodeSeq]]
  }

  class SolrService[F[_]](repo: SolrAlgebra[F]) {
    def search(query: Option[String]): F[NodeSeq] =
      repo.search(query)

    def insert(oid: Option[String]): F[Either[String, NodeSeq]] =
      repo.insert(oid)
  }
}

object Infrastructure {

  import Package._

  class SolrInterpreter[F[_]: Async : MonadLog[?[_], Chain[String]]] {
    private val servers = new CloudSolrServers(
      zkHost = "localhost:2181",
      zkClientTimeout = 15 seconds,
      zkConnectTimeout = 10 seconds,
      clusterStateUpdateInterval = 1 second,
      defaultCollection = Some("gettingstarted"),
      warmupQueries = None
    )
    private val solr = AsyncSolrClient.Builder(RoundRobinLB(servers)).build

    def search(query: Option[String]): Future[QueryResponse] = {
      blockingThreadPool.use { ec: ExecutionContext =>

        Async[F].async {
          (cb: Either[Throwable, Option[QueryResponse]] => Unit) =>

            implicit val ecc = implicitly[ExecutionContext](ec)

            val q = new SolrQuery(query.getOrElse("")).setFields("id", "name")

            val f: Future[QueryResponse] = solr.query("gettingstarted", q)

            f.onComplete {
              case Success(s) => cb(Right(s.some))
              case Failure(e) => cb(Left(e))
            }
        }
      }

    }
  }
}


