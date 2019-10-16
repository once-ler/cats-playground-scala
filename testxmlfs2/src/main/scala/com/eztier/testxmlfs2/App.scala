package com.eztier.testxmlfs2

import java.util.concurrent.Executors

import cats.{Applicative, Invariant, Semigroup, Show}
import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Sync}
import fs2.{Pipe, Stream}
import fs2.text.utf8DecodeC
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

case class DomainPlace
(
  AddressLine1: String,
  City: String,
  State: String,
  ZipCode: String
)

object Codecs {
  implicit val openMapPlaceToDomainPlace: OpenStreetPlace => DomainPlace =
    o => DomainPlace(s"${o.HouseNumber}  ${o.Road}", o.City, o.State, o.Postcode)

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

class OpenStreetMap[F[_]: ConcurrentEffect: ContextShift] {
  import Util._
  import Codecs._

  import kantan.xpath._
  import kantan.xpath.implicits._

  import com.thoughtworks.xstream._
  import com.thoughtworks.xstream.io.xml.DomDriver

  val xstream = new XStream(new DomDriver)

  // For showLinesStdOut
  implicit val showOpenStreetPlace: Show[OpenStreetPlace] = Show.show(t => s"${t.Building}\n${t.HouseNumber}\n${t.Road}\n${t.City}\n${t.State}\n${t.Postcode}\n${t.Country}")

  // Don't block the main thread
  def blockingEcStream: Stream[F, ExecutionContext] =
    Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
      Sync[F].delay(pool.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  def clientStream(ec: ExecutionContext): Stream[F, Unit] =
    clientBodyStream(ec)
      .through(placePipeS)
      .through(filterLeft) // Removes all errors
      .covary[F].showLinesStdOut

  val headers = Headers.of(Header("Accept", "*/*"))

  def createRequest: Request[F] = Request[F](
    method = Method.POST,
    uri = Uri.unsafeFromString("https://nominatim.openstreetmap.org/search?q=550+1st+Avenue,+new+york&format=xml&point=1&addressdetails=1"),
    headers = headers
  )

  def clientBodyStream(ec: ExecutionContext): Stream[F, String] =
    for {
      client <- BlazeClientBuilder[F](ec).stream
      plainRequest <- Stream.eval[F, Request[F]](Applicative[F].pure[Request[F]](createRequest))
      entityBody <- client.stream(plainRequest).flatMap(_.body.chunks).through(utf8DecodeC) // def utf8DecodeC[F[_]]: Pipe[F, Chunk[Byte], String]
    } yield entityBody


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

  def toXmlPipeS[F[_]]: Pipe[F, List[DomainPlace], List[String]] = _.map {
    places =>
      val result = places.map {
        p =>
          val xml = xstream.toXML(p)
          // println(xml)
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
