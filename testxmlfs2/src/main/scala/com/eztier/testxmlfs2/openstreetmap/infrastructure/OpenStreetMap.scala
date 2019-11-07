package com.eztier.testxmlfs2
package openstreetmap.infrastructure

import Util._
import openstreetmap.domain._
import openstreetmap.domain.Codecs._

import cats.implicits._ // syntax for show
import cats.Show
import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import fs2.{Pipe, Stream}
import org.http4s.{Method, Request, Uri}
import com.thoughtworks.xstream._
import com.thoughtworks.xstream.io.xml.DomDriver
import kantan.xpath._
import kantan.xpath.implicits._

import scala.concurrent.ExecutionContext

class OpenStreetMap[F[_]: ConcurrentEffect: ContextShift[?[_]]](val miner: Resource[F, Miner[F]]) extends WithBlockingEcStream[F] {

  val xstream = new XStream(new DomDriver)

  // For showLinesStdOut
  implicit val showOpenStreetPlace: Show[OpenStreetPlace] = Show.show(t => s"${t.Building}\n${t.HouseNumber}\n${t.Cycleway}\n${t.City}\n${t.State}\n${t.Postcode}\n${t.Country}")

  def clientStream(ec: ExecutionContext): Stream[F, Unit] =
    clientBodyStream(ec)
      .filter(_.length > 0)
      .through(placePipeS)
      .through(filterLeft) // Removes all errors
      .through(domainPlacePipeS)
      .flatMap(Stream.emits)
      .evalMap(enhanceDomainPlace)
      .flatMap(Stream.emits) // F[List[List[DomainPlace]]] to F[List[DomainPlace]]
      .flatMap(Stream.emits) // Is there a more elegant way?
      .evalMap(enhanceDomainPlaceWithSql)
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
    xp"./cycleway/text()",
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

  def enhanceDomainPlaceWithSql(place: DomainPlace) = {
    val result = miner.use {
      db =>
        db.getOnePlace(place.City).map(a => place.copy(City = a.display)).compile.toList
    }
    result
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
  def apply[F[_]: ConcurrentEffect: ContextShift](miner: Resource[F, Miner[F]]) = new OpenStreetMap(miner)
}
