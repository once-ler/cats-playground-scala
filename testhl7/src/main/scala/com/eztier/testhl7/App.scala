package com.eztier
package testhl7

import cats.data.Chain
import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {

  import algae._
  import tagless.Domain._
  import tagless.Infrastructure._

  val soap = new CkMockService[IO]
  val repo = new CkEntityInterpreter[IO](soap)
  val ckService = new CkEntityService[IO](repo)
  val ckAggregator = new CkEntityAggregator[IO](ckService)

  val epPatientAggregator = for {
    x <- createMonadLog[IO, Chain[String]]
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

  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}
