package com.eztier
package testfs2cassandra

import java.io.FileInputStream
import java.util.concurrent.Executors

import cats.effect.concurrent.Semaphore
import cats.implicits._
import cats.effect.{Blocker, Concurrent, ExitCode, IO, IOApp, Sync}
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.policies.ConstantReconnectionPolicy
import fs2.{Stream, io}

import scala.concurrent.ExecutionContext
import datasource.infrastructure.{CassandraClient, CassandraSession}
import domain._
import infrastructure._

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

  val concurrency = 5

  val cqlEndpoints = "127.0.0.1"
  val cqlPort: Int = 9042
  val user = "cassandra"
  val pass = "cassandra"

  val c = Cluster.builder()
      .addContactPoint(s"127.0.0.1")
      .withPort(cqlPort)
      .withCredentials(user, pass)
      .withReconnectionPolicy(new ConstantReconnectionPolicy(5000))
      .build()

  override def run(args: List[String]): IO[ExitCode] = {
    val r = (for {
      s <- Semaphore[IO](concurrency)
      cs = CassandraSession[IO](cqlEndpoints, cqlPort, user.some, pass.some).getSession
      cl = CassandraClient(cs)
      ci = CassandraInterpreter(cl)
      tx = new TextExtractInterpreter[IO](concurrency, s, ci)
    } yield (ci, tx))
      .unsafeRunSync()

      r._1.runTest.compile.drain.as(ExitCode.Success)

/*
      r._2
        .initialize
        .aggregate(src).compile.drain.as(ExitCode.Success)

*/
  }
}