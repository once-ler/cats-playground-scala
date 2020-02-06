package com.eztier
package testfs2cassandra

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.datastax.driver.core.Cluster
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

  override def run(args: List[String]): IO[ExitCode] = {

    // Get DocumentAggregator resource.
    val db = for {
      da <- initializeDocumentAggregatorResource[IO]
    } yield da

    val program = db.use {
      case documentAggregator =>
        // println("Connected")

        /*
        // Generate metadata.
        documentAggregator
          .getDocumentXml
          .compile.drain.unsafeRunSync()
        */

        /*
        // Extract from disk and persist to cassandra.
        documentAggregator
          .extractDocument
          .compile.drain.unsafeRunSync()
        */

        // Add later because doc_name, doc_date_created, and doc_year_created were missed.
        // Partial update of some fields b/c missed from previous import.
        documentAggregator
          .partialUpdate
          .compile.drain.unsafeRunSync()

        IO.unit
    }

    IO(program.unsafeRunSync()).as(ExitCode.Success)


  }

  def previousRun = {

    /*
    val concurrency = 5
    val cqlEndpoints = "127.0.0.1"
    val cqlPort: Int = 9042
    val user = "cassandra"
    val pass = "cassandra"

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

/*
    // Get TextExtractor & CassandraInterpreter resources.
    val db1 = for {
      ci <- initializeCassandraResource[IO]
      tx <- initializeTextExtractorResource[IO]
    } yield (ci, tx)

    val program1 = db1.use {
      case (ci, tx) =>

        IO.unit
    }

    IO(program1.unsafeRunSync()).as(ExitCode.Success)
*/
  }

}