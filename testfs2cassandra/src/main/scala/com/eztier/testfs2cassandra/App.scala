package com.eztier.testfs2cassandra

import java.io.FileInputStream
import java.util.concurrent.Executors

import cats.implicits._
import cats.effect.{Blocker, Concurrent, ExitCode, IO, IOApp, Sync}
import fs2.{Stream, io}

import scala.concurrent.ExecutionContext

import infrastructure._

object App extends IOApp {

  val files = List(
    ("doc1", "/home/htao/tmp/fourth-grade-spelling-words.pdf"),
    ("doc2", "/home/htao/tmp/sparkcontext-examples.pdf"),
    ("doc3", "/home/htao/Pictures/chat-demo-split-view.png"),
    ("doc4", "/home/htao/Pictures/username-already-used.png")
  )




  override def run(args: List[String]): IO[ExitCode] = ???
}