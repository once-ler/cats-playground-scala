package com.eztier.testxmlfs2

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.eztier.testxmlfs2.openstreetmap.infrastructure.OpenStreetMap
import fs2.{Stream, io, text}
import java.nio.file.Paths

class XmlService[F[_]: Sync : ContextShift] {

  def read: Stream[F, String] = Stream.resource(Blocker[F]).flatMap { blocker =>
    io.file.readAll[F](Paths.get("resources/patients.xml"), blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
  }

}

object App extends IOApp {
  val miner = Database.getMiner[IO]

  def run(args: List[String]): IO[ExitCode] = (OpenStreetMap[IO](miner)).run.as(ExitCode.Success)
}

/*
Result:
f
List(<DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Kips Bay, Manhattan Community Board 6, Manhattan, New York County, NYC, New York, 10016, USA</Neighborhood>
</DomainPlace>, <DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Midtown South, Manhattan Community Board 5, Manhattan, New York County, NYC, New York, 10016, United States of America</Neighborhood>
</DomainPlace>)

 */
