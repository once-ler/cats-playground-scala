package com.eztier.testfs2pubsub

import scala.concurrent.duration._
// import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect._
import cats.syntax.all._ // For .as(ExitCode.Success)
import fs2.Stream

object App extends IOApp {
  // An infinite stream of the periodic elapsed time
  val seconds = Stream.awakeEvery[IO](1.second).map(_ => (System.currentTimeMillis() % 10000).toString)

  def run(args: List[String]): IO[ExitCode] =
    seconds.compile.drain.as(ExitCode.Success)
}
