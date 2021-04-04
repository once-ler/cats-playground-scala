package com.eztier
package testhl7

import cats.implicits._
import cats.data.Chain
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import scala.concurrent.duration._
import fs2.Stream

object Tests {
  def taglessTest0 = {
    val ec = scala.concurrent.ExecutionContext.global
    implicit val timer = IO.timer(ec)
    implicit val cs = IO.contextShift(ec)  // Need cats.effect.ContextShift[cats.effect.IO] because NOT inside of IOApp

    // import algae._
    import com.eztier.common.MonadLog

    import tagless.Domain._
    import tagless.Infrastructure._

    val soap = new CkMockService[IO]
    val repo = new CkEntityInterpreter[IO](soap)
    val ckService = new CkEntityService[IO](repo)
    val ckAggregator = new CkEntityAggregator[IO](ckService)

    val epPatientAggregator = for {
      // x <- createMonadLog[IO, Chain[String]]
      x <- MonadLog.createMonadLog[IO, String]
      y = new EpPatientAggregator[IO](x)
    } yield y

    val response2 = epPatientAggregator.unsafeRunSync().getOrCreateEntity(ckAggregator, Some("Foo")).unsafeRunSync()

    println(response2)
    /*
    Sorry!
    Sorry!
    Sorry!
    Sorry!
    CkParticipantAggregate(Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)))
    */

    // val response3 = epPatientAggregator.getOrCreateEntityF(ckAggregator, Some("Foo")).unsafeRunSync()
    val response3 = epPatientAggregator.unsafeRunSync().getOrCreateEntityF(ckAggregator, Some("Foo")).unsafeRunSync()

    println(response3)
    /*
    Sorry!
    Sorry!
    Sorry!
    Sorry!
    Chain(Sorry!, Sorry!, Sorry!, Sorry!)
    CkParticipantAggregate(None,None,None,None)
    */
  }
}


object App extends IOApp {
  import tagless.multi.Ck.Package.createCkAggregatorResource
  import tagless.multi.Ep.Package.createEpPatientAggregatorResource
  import tagless.multi.Solr.Package.createSolrResource

  val r = for {
    r0 <- createEpPatientAggregatorResource[IO]
    r1 <- createCkAggregatorResource[IO]
    r2 <- createSolrResource[IO]
  } yield (r0, r1, r2)

  /*
  r.use {
    case (src, dest) =>

      val response4 = src.getOrCreateEntityF(dest, Some("Foo")).unsafeRunSync()

      // The log shows nothing because the error occurred in the log of the dest domain.
      println(response4)

      val destLog = dest.getMonadLogStorage.unsafeRunSync()

      // The log belongs to the dest domain.
      println(destLog.show)

      IO(println("Done"))
  }.unsafeRunSync()
  */

  /*
  r.use {
    case (src, dest, solr) =>

      def pause[F[_]: Timer](d: FiniteDuration) = Stream.emit(1).covary[F].delayBy(d)

      def repeat(io : IO[Unit]): IO[Nothing] = IO.suspend(io *> IO.delay(
        for {
          _ <- IO( src.runUnprocessed(dest, Some("Foo")).compile.drain.unsafeRunSync() )
          _ <- IO( src.run.compile.drain.unsafeRunSync() )
        } yield pause(5 seconds).compile.drain.unsafeRunSync()
      ).unsafeRunSync() *> repeat(io))

      repeat(IO.delay(println("Start"))).unsafeRunSync()

      IO.unit
  }.unsafeRunSync()
  */

  r.use {
    case (src, dest, solr) =>

      val res = solr.search(collection = "gettingstarted", query = "name:*", fields = List("id", "name")).unsafeRunSync()

      res.get.getResults.forEach(a => println(a.jsonStr()))

      solr.shutdown

      IO.unit
  }.unsafeRunSync()

  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}
