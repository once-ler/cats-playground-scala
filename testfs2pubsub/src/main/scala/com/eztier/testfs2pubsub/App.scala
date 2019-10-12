package com.eztier.testfs2pubsub

import fs2.{Pull, Stream}
import fs2.concurrent.Topic

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Sync}
// import cats.syntax.all._ // Do not import cats.syntax.all._ or .as(ExitCode.Success) will not work
import cats.implicits._ // For Show[I_]

object App extends IOApp {
  // https://fs2.io/concurrency-primitives

  // -----------------------------------------------------------------------------------------------
  def doNothing = {
    // val ec = scala.concurrent.ExecutionContext.global
    // implicit val timer = IO.timer(ec)
    // implicit val cs = IO.contextShift(ec)  // Do not need cats.effect.ContextShift[cats.effect.IO] because inside of IOApp

    def repeat(io : IO[Unit]): IO[Nothing] = IO.suspend(io *> IO.sleep(1.second) *> repeat(io))

    val printFoo: IO[Unit] = IO.delay(println(s"foo"))

    val app = for {
      _ <- IO.delay(println("Basic Setup..."))
      fooThread <- repeat(printFoo).start // runs the computation in the background
      _ <- IO.sleep(10.seconds) *> IO.delay(println("Rest of the app..."))
      _ <- fooThread.cancel // stops foo when you're done
    } yield ()

    // app.unsafeRunSync()

    Stream.eval(app)
  }

  def run4(args: List[String]): IO[ExitCode] = doNothing.compile.drain.as(ExitCode.Success)

  // -----------------------------------------------------------------------------------------------
  // Topic.apply expects (implicit F: Concurrent[F])
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

  def run3(args: List[String]): IO[ExitCode] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val sharedTopic = sharedTopicStream[IO]("TOPIC_A")

    // sharedTopic is passed to your Services, which use it as necessary
    val program = for {
      _ <- sharedTopic.flatMap {
        topic =>
          requestAddPublisher("publisher1", topic) concurrently
          requestAddPublisher("publisher2", topic) concurrently
          requestAddSubscriber(topic) concurrently
          requestAddSubscriber(topic)
        }
      } yield ()

    program.compile.drain.as(ExitCode.Success)
  }

  // Infinite Pull loop (Find usecase for this)
  def loop[F[_],O,R](using: R => Pull[F,O,Option[R]]): R => Pull[F,O,Option[R]] =
    r => using(r) flatMap { _.map(loop(using)).getOrElse(Pull.pure(None)) }

  // An infinite stream of the periodic elapsed time
  val seconds = Stream.awakeEvery[IO](1.second).map(_ => (System.currentTimeMillis() % 10000).toString)

  def run2(args: List[String]): IO[ExitCode] =
    seconds.compile.drain.as(ExitCode.Success)

  // -----------------------------------------------------------------------------------------------
  // Concurrent and Parallel execution.
  private def isPrime(n: Long): (Long, Boolean) = n match {
    case 1 => (1, true)
    case n =>
      val sqn = Math.sqrt(n)
      var i = 2
      while (i <= sqn) {
        if (n % i == 0) return (n, false)
        i += 1
      }
      (n, true)
  }

  // Concurrent[?[_]] == R[F[_]] = Concurrent[F, A]
  def concurrentParallelExecution[F[_]: Sync: Concurrent[?[_]]](n: Int): Stream[F, Unit] = {
    val concurrency = 4
    val offset: Long = 9138000000L
    val length = 1000000 / n

    val data: Stream[F, Stream[F, Int]] = Stream.range(0, n).map(i => Stream.range(i * length, (i + 1) * length))

    // implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
    // implicit val concurrent = IO.ioConcurrentEffect(IO.contextShift(ec))

    val computations: Stream[F, (Long, Boolean)] =
      data.map(_.evalMap { el =>

        // IO(isPrime(el + offset))

        Sync[F].delay(isPrime(el + offset))

      }).parJoin(concurrency)

    // computations.compile.toVector.unsafeRunSync

    computations.covary[F].showLinesStdOut
  }

  def run(args: List[String]): IO[ExitCode] = concurrentParallelExecution[IO](8).compile.drain.as(ExitCode.Success)


}
