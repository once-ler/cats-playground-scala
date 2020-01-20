package com.eztier
package testfs2cassandra

import java.io.FileInputStream
import java.util.concurrent.Executors

import cats.Applicative
import io.circe.config.{parser => ConfigParser}
import cats.effect.concurrent.Semaphore
import cats.implicits._
import cats.effect.{Async, Blocker, Concurrent, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import com.datastax.driver.core.{BatchStatement, BoundStatement, Cluster, PreparedStatement}
import com.datastax.driver.core.policies.ConstantReconnectionPolicy
import com.eztier.datasource.infrastructure.cassandra.{CassandraClient, CassandraSession}
import fs2.Stream
import domain._
import infrastructure._
import config._
import com.eztier.datasource.infrastructure.cassandra.{CassandraClient, CassandraSession}
import doobie.util.ExecutionContexts

object App extends IOApp {

  val filesK = List(
    Extracted(domain="domain1".some, root_type="A".some, root_id="1234".some,
      doc_id="doc1".some,
      doc_file_path="/home/htao/tmp/fourth-grade-spelling-words.pdf".some),
    Extracted(domain="domain1".some, root_type="A".some, root_id="1234".some,
      doc_id="doc2".some,
      doc_file_path="/home/htao/tmp/sparkcontext-examples.pdf".some),
    Extracted(domain="domain2".some, root_type="A".some, root_id="abcd".some,
      doc_id="doc3".some,
      doc_file_path="/home/htao/tmp/fourth-grade-spelling-words.pdf".some),
    Extracted(domain="domain2".some, root_type="A".some, root_id="abcd".some,
      doc_id="doc4".some,
      doc_file_path="/home/htao/tmp/fourth-grade-spelling-words.pdf".some),
    Extracted(domain="domain2".some, root_type="A".some, root_id="abcd".some,
      doc_id="doc5".some,
      doc_file_path="/home/htao/tmp/fourth-grade-spelling-words.pdf".some)
  )

  val src = Stream.emits(filesK)
  val src2 = Stream.emits(1 to 10)

  val concurrency = 5

  val cqlEndpoints = "127.0.0.1"
  val cqlPort: Int = 9042
  val user = "cassandra"
  val pass = "cassandra"

  /*
  val c = Cluster.builder()
      .addContactPoint(s"127.0.0.1")
      .withPort(cqlPort)
      .withCredentials(user, pass)
      .withReconnectionPolicy(new ConstantReconnectionPolicy(5000))
      .build()
  */


  def initializeDbResource[F[_]: Async : Applicative: ContextShift] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testfs2cassandra"))
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.db.eventstore))
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.eventstore.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf.db.eventstore, connEc, Blocker.liftExecutionContext(txnEc))
      documentMetadataRepo = new DoobieDocumentMetataInterpreter[F](xa)
      documentMetadataService = new DocumentMetadataService[F](documentMetadataRepo)
      documentRepo = new DoobieDocumentInterpreter[F](xa)
      documentService = new DocumentService[F](documentRepo)
    } yield (documentMetadataService, documentService)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val db = initializeDbResource[IO].use {
      case (documentMetadataService, documentService) =>
        println("Connected")
        IO.unit
    }

    db.unsafeRunSync()

    IO(ExitCode.Success)

/*
    val r = (for {
      s <- Semaphore[IO](concurrency)
      cs = CassandraSession[IO](cqlEndpoints, cqlPort, user.some, pass.some).getSession
      cl = CassandraClient(cs)
      ci = CassandraInterpreter(cl)
      tx = new TextExtractInterpreter[IO](concurrency, s, ci)
      hi = new HttpInterpreter[IO]()
    } yield (ci, tx, hi))
      .unsafeRunSync()

      r._3.runConcurrentTest(src2)
      .handleErrorWith { e =>
        println(e.getMessage())

        println("Onwards... ")
        Stream.eval(().pure[IO])
      }
      .compile.drain.as(ExitCode.Success)
*/
      // r._1.runCreateTest.compile.drain.as(ExitCode.Success)

/*
      r._2
        .initialize
        .aggregate(src).compile.drain.as(ExitCode.Success)

*/
  }
}