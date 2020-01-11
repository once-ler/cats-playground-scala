package com.eztier
package testhl7.tagless.multi
package Solr

import java.io.File
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
import com.eztier.testhl7.tagless.multi.Solr.Config.DatabaseConfig
import com.eztier.testhl7.tagless.multi.Solr.Domain.SolrAlgebra
import io.chrisdavenport.log4cats.Logger
import common.CatsLogger._
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.config.{parser => ConfigParser}
import io.ino.solrs.{AsyncSolrClient, CloudSolrServers, RequestInterceptor, RetryPolicy, RoundRobinLB, SolrServer, future}
import org.apache.solr.client.solrj.SolrRequest.METHOD.GET
import org.apache.solr.client.solrj.request.{ContentStreamUpdateRequest, QueryRequest}
import org.apache.solr.client.solrj.{SolrQuery, SolrRequest, SolrResponse, StreamingResponseCallback}
import org.apache.solr.client.solrj.response.{QueryResponse, UpdateResponse}
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.{ModifiableSolrParams, SolrParams}
// import org.apache.solr.handler.extraction
import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig, Realm}

object Package {
  import Domain._
  import Infrastructure._
  import Config._

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newFixedThreadPool(3)
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createSolrResource[F[_]: Async :ContextShift :ConcurrentEffect: Timer] = {
    for {
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(createMonadLog[F, Chain[String]])
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testhl7")) // Lifts an applicative into a resource.
      entityRepo = new SolrInterpreter[F](conf.solr)
      entityService = new SolrService(entityRepo)
    } yield entityService
  }
}

object Config {
  implicit val dbconnDecoder: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbDecoder: Decoder[DatabaseConfig] = deriveDecoder
  implicit val appDecoder: Decoder[AppConfig] = deriveDecoder

  case class DatabaseConnectionsConfig(poolSize: Int)

  case class DatabaseConfig(
    url: String,
    driver: String,
    user: String,
    password: String,
    connections: DatabaseConnectionsConfig
  )

  final case class AppConfig(db: DatabaseConfig, solr: DatabaseConfig)


}

object Domain {
  trait SolrAlgebra[F[_]] {
    def search(collection: String, query: String, fields: List[String]): F[Option[QueryResponse]]

    def insert(oid: Option[String]): F[Either[String, Unit]]

    def shutdown: Unit
  }

  class SolrService[F[_]](repo: SolrAlgebra[F]) {
    def search(collection: String, query: String, fields: List[String]): F[Option[QueryResponse]] =
      repo.search(collection, query, fields )

    def insert(oid: Option[String]): F[Either[String, NodeSeq]] =
      repo.insert(oid)

    def shutdown = repo.shutdown
  }
}

object Infrastructure {

  import Package._
  import Config._

  class SolrInterpreter[F[_]: Async : MonadLog[?[_], Chain[String]]](conf: DatabaseConfig) extends SolrAlgebra[F] {
    private val servers = new CloudSolrServers(
      zkHost = "localhost:2181/solr",
      zkClientTimeout = 15 seconds,
      zkConnectTimeout = 10 seconds,
      clusterStateUpdateInterval = 1 second,
      defaultCollection = None,
      warmupQueries = None
    )

    private val reqInterceptor = new RequestInterceptor {
      override def interceptRequest[T <: SolrResponse](f: (SolrServer, SolrRequest[_ <: T]) => future.Future[T])(solrServer: SolrServer, r: SolrRequest[_ <: T]): future.Future[T] = {
        f(solrServer, r).map {
          qr =>
            qr
        }
      }
    }

    private val realm = new Realm.Builder(conf.user, conf.password)
      .setScheme(Realm.AuthScheme.BASIC)
      .setUsePreemptiveAuth(true)
      .build()

    def createDefaultHttpConfig: DefaultAsyncHttpClientConfig = {
      val clientConfigBuilder = new DefaultAsyncHttpClientConfig.Builder
      clientConfigBuilder.setRealm(realm)
      clientConfigBuilder.build()
    }

    private val httpClient = new DefaultAsyncHttpClient(createDefaultHttpConfig)

    private val solr = AsyncSolrClient.Builder(RoundRobinLB(servers))
      // .withRequestInterceptor(reqInterceptor)
      .withHttpClient(httpClient)
      .withRetryPolicy(RetryPolicy.TryAvailableServers).build

    private def queryParams(collection: Option[String], q: Option[SolrParams]): ModifiableSolrParams = {
      val reqParams = new ModifiableSolrParams(q.orNull)
      collection.foreach(reqParams.set("collection", _))
      reqParams
    }

    override def search(collection: String, query: String, fields: List[String]): F[Option[QueryResponse]] = {
      blockingThreadPool.use { ec: ExecutionContext =>

        Async[F].async {
          (cb: Either[Throwable, Option[QueryResponse]] => Unit) =>

            implicit val ecc = implicitly[ExecutionContext](ec)

            // val q = new SolrQuery(query).setFields("id", "name").setRows(10)

            val q = new SolrQuery(query).setRows(10)

            fields.foreach(q.addField(_))

            val f: Future[QueryResponse] = solr.query(collection, q)

            f.onComplete {
              case Success(s) => cb(Right(s.some))
              case Failure(e) => cb(Left(e))
            }
        }
      }
    }

    override def insert(oid: Option[String]) =
      blockingThreadPool.use { ec: ExecutionContext =>
        Async[F].async {
          (cb: Either[Throwable, Unit] => Unit) =>
            implicit val cs = ec

            val doc1 = new SolrInputDocument()
            doc1.addField("id", "id1")
            doc1.addField("name", "doc1")

            val f = for {
              _ <- solr.addDocs(docs = Iterable(doc1))
              _ <- solr.commit()
            } yield ()

            f.onComplete {
              case _ => cb()
            }
        }
      }

    def insertRichDoc(): F[UpdateResponse] =
      blockingThreadPool.use { ec: ExecutionContext =>
        Async[F].async {
          (cb: Either[Throwable, UpdateResponse] => Unit) =>

            implicit val cx = ec

            val req = new ContentStreamUpdateRequest("/update/extract")
            req.addFile(new File("my-file.pdf"), null)
            // req.setParam("extractOnly", "true")

            val f = solr.execute(req)

            f.onComplete {
              case Success(s) => cb(Right(s))
              case Failure(e) => cb(Left(e))
            }
        }
      }

    override def shutdown: Unit = solr.shutdown()

  }
}


