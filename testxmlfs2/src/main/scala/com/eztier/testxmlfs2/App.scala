package com.eztier.testxmlfs2

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.eztier.testxmlfs2.openstreetmap.infrastructure.OpenStreetMap

object App extends IOApp {
  val miner = Database.getMiner[IO]

  def run(args: List[String]): IO[ExitCode] = (OpenStreetMap[IO](miner)).run.as(ExitCode.Success)
}

/*
Result:

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
