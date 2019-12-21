package com.eztier
package testhl7

import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {

  import tagless.Domain._
  import tagless.Infrastructure._

  val soap = new CkMockService[IO]
  val repo = new CkEntityInterpreter[IO](soap)
  val ckService = new CkEntityService[IO](repo)
  val ckAggregator = new CkEntityAggregator[IO](ckService)
  val epPatientAggregator = new EpPatientAggregator[IO]

  val response2 = epPatientAggregator.getOrCreateEntity(ckAggregator, Some("Foo")).unsafeRunSync()

  println(response2)
  /*
  Sorry!
  Sorry!
  Sorry!
  Sorry!
  CkParticipantAggregate(Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)),Some(GetEntityByIDResponse(None)))
  */

  val response3 = epPatientAggregator.getOrCreateEntityF(ckAggregator, Some("Foo")).unsafeRunSync()

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
