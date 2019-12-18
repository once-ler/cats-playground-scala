package com.eztier
package testhl7

import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import Infrastructure.SomeResponseApi
import com.eztier.testhl7.Domain.SomeResponse

object App extends IOApp {

  val response = new AsyncTest[IO].run.unsafeRunSync()

  println(response.note)

  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}
