package com.eztier.testxmlfs2

import java.util.concurrent.Executors

import cats.{Applicative, Invariant, Semigroup, Show}
import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Sync}
import fs2.{Pipe, Stream}
import fs2.text.utf8DecodeC
import cats.implicits._
import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations._
import org.http4s.{Header, Headers, Method, Request, Uri}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

case class OpenStreetPlace
(
  Building: String,
  HouseNumber: String,
  Road: String,
  Suburb: String,
  City: String,
  County: String,
  State: String,
  Postcode: String,
  Country: String,
  CountryCode: String
)

case class OpenStreetNeighborhood
(
  PlaceId: String,
  PlaceRank: String,
  Boundingbox: String,
  Lat: String,
  Lon: String,
  DisplayName: String,
  Class: String,
  Type: String,
  importance: String
)

case class DomainPlace
(
  AddressLine1: String = "",
  City: String = "",
  State: String = "",
  ZipCode: String = "",
  Neighborhood: String = ""
)

object Codecs {
  implicit val openMapPlaceToDomainPlace: OpenStreetPlace => DomainPlace =
    o => DomainPlace(s"${o.HouseNumber} ${o.Road}", o.City, o.State, o.Postcode)

  implicit val domainPlaceToOpenMapPlace: DomainPlace => OpenStreetPlace =
    d => OpenStreetPlace(
      "",
      d.AddressLine1.split(' ').headOption.getOrElse(""),
      d.AddressLine1.split(' ').drop(1).mkString(" "),
      "",
      d.City,
      "",
      d.State,
      d.ZipCode,
      "United States of America",
      "us"
    )

  implicit val openMapNeighborhoodToDomainPlace: OpenStreetNeighborhood => DomainPlace =
    o => DomainPlace("", "", "", "", o.DisplayName)


  /*
  implicit val openStreetPlaceSemigroup: Semigroup[OpenStreetPlace] = new Semigroup[OpenStreetPlace] {
    def combine(a: OpenStreetPlace, b: OpenStreetPlace): OpenStreetPlace = ???
  }

  implicit val semigroupPlace: Semigroup[DomainPlace] =
    Semigroup[OpenStreetPlace].imap(openMapPlaceToDomainPlace)(domainPlaceToOpenMapPlace)
  */
}

object Util {
  def filterLeft[F[_], A, B]: Pipe[F, Either[A, B], B] = _.flatMap {
    case Right(r) => Stream.emit(r)
    case Left(_) => Stream.empty
  }

  def filterRight[F[_], A, B]: Pipe[F, Either[A, B], A] = _.flatMap {
    case Left(e) => Stream.emit(e)
    case Right(_) => Stream.empty
  }
}

abstract class WithBlockingEcStream[F[_]: ConcurrentEffect: ContextShift] {
  val headers = Headers.of(Header("Accept", "*/*"))

  // Don't block the main thread
  def blockingEcStream: Stream[F, ExecutionContext] =
    Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
      Sync[F].delay(pool.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  def createRequest(zipCode: String = ""): Request[F]

  def clientBodyStream(ec: ExecutionContext, zipCode: String = ""): Stream[F, String] =
    for {
      client <- BlazeClientBuilder[F](ec).stream
      plainRequest <- Stream.eval[F, Request[F]](Applicative[F].pure[Request[F]](createRequest(zipCode)))
      entityBody <- client.stream(plainRequest).flatMap(_.body.chunks).through(utf8DecodeC) // def utf8DecodeC[F[_]]: Pipe[F, Chunk[Byte], String]
    } yield entityBody

}

class OpenStreetMapNeighborhood[F[_]: ConcurrentEffect: ContextShift] extends WithBlockingEcStream[F] {
  import Util._
  import Codecs._

  import kantan.xpath._
  import kantan.xpath.implicits._

  implicit val neighborhoodDecoder: NodeDecoder[OpenStreetNeighborhood] = NodeDecoder.decoder(
    xp"./@place_id",
    xp"./@place_rank",
    xp"./@boundingbox",
    xp"./@lat",
    xp"./@lon",
    xp"./@display_name",
    xp"./@class",
    xp"./@type",
    xp"./@importance"
  )(OpenStreetNeighborhood.apply)

  def clientStream(ec: ExecutionContext, place: DomainPlace = DomainPlace()): Stream[F, List[DomainPlace]] =
    clientBodyStream(ec, place.ZipCode)
      .through(neighborhoodPipeS)
      .through(filterLeft) // Removes all errors
      .through(domainPlacePipeS(place))

  def createRequest(zipCode: String): Request[F] = Request[F](
    method = Method.GET,
    uri = Uri.unsafeFromString(s"https://nominatim.openstreetmap.org/search?postalcode=$zipCode&country=us&format=xml&point=1&addressdetails=0"),
    headers = headers
  )

  def neighborhoodPipeS[F[_]]: Pipe[F, String, Either[XPathError, List[OpenStreetNeighborhood]]] = _.map {
    str =>
      val result: kantan.xpath.XPathResult[List[OpenStreetNeighborhood]] = str.evalXPath[List[OpenStreetNeighborhood]](xp"//place")
      result
  }

  def domainPlacePipeS[F[_]](place: DomainPlace = DomainPlace()): Pipe[F, List[OpenStreetNeighborhood], List[DomainPlace]] = _.map {
    places =>
      val result = places.map {
        p =>
          val d: DomainPlace = p

          place.copy(Neighborhood = d.Neighborhood)
      }
      result
  }
}

class OpenStreetMap[F[_]: ConcurrentEffect: ContextShift] extends WithBlockingEcStream[F] {
  import Util._
  import Codecs._

  import kantan.xpath._
  import kantan.xpath.implicits._

  import com.thoughtworks.xstream._
  import com.thoughtworks.xstream.io.xml.DomDriver

  val xstream = new XStream(new DomDriver)

  // For showLinesStdOut
  implicit val showOpenStreetPlace: Show[OpenStreetPlace] = Show.show(t => s"${t.Building}\n${t.HouseNumber}\n${t.Road}\n${t.City}\n${t.State}\n${t.Postcode}\n${t.Country}")


  def clientStream(ec: ExecutionContext): Stream[F, Unit] =
    clientBodyStream(ec)
      .through(placePipeS)
      .through(filterLeft) // Removes all errors
      .through(domainPlacePipeS)
      .flatMap(Stream.emits)
      .evalMap(enhanceDomainPlace)
      .flatMap(Stream.emits) // F[List[List[DomainPlace]]] to F[List[DomainPlace]]
      .through(toXmlPipeS)
      .covary[F].showLinesStdOut

  def createRequest(zipCode: String = ""): Request[F] = Request[F](
    method = Method.GET,
    uri = Uri.unsafeFromString("https://nominatim.openstreetmap.org/search?q=550+1st+Avenue,+new+york&format=xml&point=1&addressdetails=1"),
    headers = headers
  )

  implicit val placeDecoder: NodeDecoder[OpenStreetPlace] = NodeDecoder.decoder(
    xp"./building/text()",
    xp"./house_number/text()",
    xp"./road/text()",
    xp"./suburb/text()",
    xp"./city/text()",
    xp"./county/text()",
    xp"./state/text()",
    xp"./postcode/text()",
    xp"./country/text()",
    xp"./country_code/text()"
  )(OpenStreetPlace.apply)

  def placePipeS[F[_]]: Pipe[F, String, Either[XPathError, List[OpenStreetPlace]]] = _.map {
    str =>
      val result: kantan.xpath.XPathResult[List[OpenStreetPlace]] = str.evalXPath[List[OpenStreetPlace]](xp"//place")
      result
  }

  def domainPlacePipeS[F[_]]: Pipe[F, List[OpenStreetPlace], List[DomainPlace]] = _.map {
    places =>
      val result = places.map {
        p =>
          val d: DomainPlace = p
          d
      }
      result
  }

  def enhanceDomainPlace(place: DomainPlace) = {
    val f = for {
      ec <- blockingEcStream
      s <- (new OpenStreetMapNeighborhood[F]).clientStream(ec, place)
    } yield s

    f.compile.toList
  }

  def toXmlPipeS[F[_]]: Pipe[F, List[DomainPlace], List[String]] = _.map {
    places =>
      val result = places.map {
        p =>
          // For nested types.
          // val placeClazz = classOf[DomainPlace]
          // xstream.useAttributeFor(placeClazz, "DomainPlace")

          xstream.aliasPackage("", "com.eztier.testxmlfs2")
          val xml = xstream.toXML(p)

          xml
      }
      result
  }

  def run: F[Unit] =
    blockingEcStream.flatMap { blockingEc =>
      clientStream(blockingEc)
    }.compile.drain

}

object OpenStreetMap {
  def apply[F[_]: ConcurrentEffect: ContextShift] = new OpenStreetMap()
}


object App extends IOApp {
  def run(args: List[String]): IO[ExitCode] = (OpenStreetMap[IO]).run.as(ExitCode.Success)
}

/*
Result:

List(<DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>NYC</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Kips Bay, Manhattan Community Board 6, Manhattan, New York County, NYC, New York, 10016, USA</Neighborhood>
</DomainPlace>, <DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>NYC</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Midtown South, Manhattan Community Board 5, Manhattan, New York County, NYC, New York, 10016, United States of America</Neighborhood>
</DomainPlace>)

 */
