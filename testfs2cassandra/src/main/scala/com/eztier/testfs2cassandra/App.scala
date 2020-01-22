package com.eztier
package testfs2cassandra

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

// import java.io.FileInputStream
// import java.util.concurrent.Executors
// import scala.concurrent.duration._
// import cats.Applicative
// import cats.effect.concurrent.Semaphore
// import cats.effect.{Async, Blocker, Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
// import com.datastax.driver.core.{BatchStatement, BoundStatement, Cluster, PreparedStatement}
// import com.datastax.driver.core.policies.ConstantReconnectionPolicy
// import doobie.util.ExecutionContexts
// import io.circe.config.{parser => ConfigParser}

import domain._
import infrastructure._
import config._
import com.eztier.datasource.infrastructure.cassandra.{CassandraClient, CassandraSession}

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

  override def run(args: List[String]): IO[ExitCode] = {

    val db = initializeDbResource[IO].use {
      case documentAggregator =>
        // println("Connected")

        documentAggregator
          .getDocumentXml
          .showLinesStdOut
          .compile.drain.unsafeRunSync()

        IO.unit
    }

    // val db = (Stream.awakeEvery[IO](0.25.second) zipRight Stream.emits(1 to 100)).showLinesStdOut.compile.drain

    IO(db.unsafeRunSync()).as(ExitCode.Success)

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