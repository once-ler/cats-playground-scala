package com.eztier.testfs2pubsub


import fs2.{Pull, Sink, Stream, io, text}
import fs2.concurrent.Topic

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.effect._
import cats.syntax.all._
import cats.implicits._ // For Show[I_]

object App extends IOApp {

  def doNothing = {
    val ec = scala.concurrent.ExecutionContext.global
    implicit val timer = IO.timer(ec)
    implicit val cs = IO.contextShift(ec)

    def repeat(io : IO[Unit]) : IO[Nothing] = io *> IO.sleep(1.second) *> repeat(io)

    val printFoo: IO[Unit] = IO.delay(println(s"foo"))

    val app = for {
      _ <- IO.delay(println("Basic Setup..."))
      fooThread <- repeat(printFoo).start // runs the computation in the background
      _ <- IO.delay(println("Rest of the app..."))
      _ <- fooThread.cancel // stops foo when you're done
    } yield ()

    app.unsafeRunSync()
  }

  // Topic.appy expects (implicit F: Concurrent[F])
  // Using kind projector will produce concurrent[?[_]] at compile time.
  def sharedTopicStream[F[_]: Concurrent[?[_]]](topicId: String)(implicit ec: ExecutionContext): Stream[F, Topic[F, String]] =
    Stream.eval(Topic[F, String](s"Topic $topicId start"))

  /** covary[F[_]] lifts this stream to the specified effect type. */
  // def covary[F[_]]: Stream[F, O] = self
  def addPublisher[F[_]](topic: Topic[F, String], value: String): Stream[F, Unit] =
    Stream.emit(value).covary[F].repeat.through(topic.publish)

  def addSubscriber[F[_]](topic: Topic[F, String]): Stream[F, String] =
    topic
      .subscribe(10) // maxQueued
      .take(4)

  // a request that adds a publisher to the topic
  def requestAddPublisher[F[_]](value: String, topic: Topic[F, String]): Stream[F, Unit] =
    addPublisher(topic, value)

  // a request that adds a subscriber to the topic
  def requestAddSubscriber[F[_]](topic: Topic[F, String])(implicit F: Effect[F]): Stream[F, Unit] =
    addSubscriber(topic).through(a => a.covary[F].showLinesStdOut)

  def run(args: List[String]): IO[ExitCode] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val sharedTopic = sharedTopicStream[IO]("TOPIC_A")

    // sharedTopic is passed to your Services, which use it as necessary
    sharedTopic.flatMap {
      topic =>
        requestAddPublisher("publisher1", topic) concurrently
          requestAddPublisher("publisher2", topic) concurrently
          requestAddSubscriber(topic) concurrently
          requestAddSubscriber(topic)
    }.compile.drain.as(ExitCode.Success)
  }


  // Infinite Pull loop
  def loop[F[_],O,R](using: R => Pull[F,O,Option[R]]): R => Pull[F,O,Option[R]] =
    r => using(r) flatMap { _.map(loop(using)).getOrElse(Pull.pure(None)) }

  // An infinite stream of the periodic elapsed time
  val seconds = Stream.awakeEvery[IO](1.second).map(_ => (System.currentTimeMillis() % 10000).toString)

  def run2(args: List[String]): IO[ExitCode] =
    seconds.compile.drain.as(ExitCode.Success)
}
