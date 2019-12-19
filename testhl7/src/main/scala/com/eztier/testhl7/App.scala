package com.eztier
package testhl7

import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import Infrastructure.SomeResponseApi
import com.eztier.testhl7.Domain.SomeResponse

object App extends IOApp {

  val response = new AsyncTest[IO].run.unsafeRunSync()

  println(response.note)

  import tagless.Domain._
  import tagless.Infrastructure._

  val soap = new CkMockService[IO]
  val repo = new CkEntityInterpreter[IO](soap)
  val ckService = new CkEntityService[IO](repo)
  val ckAggregator = new CkEntityAggregator[IO](ckService)
  val epPatientAggregator = new EpPatientAggregator[IO]

  val response2 = epPatientAggregator.getOrCreateEntity(ckAggregator, Some("Foo")).unsafeRunSync()

  println(response2)

  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}
