package com.eztier.testxmlfs2
package openstreetmap.infrastructure

import Util._
import openstreetmap.domain._
import openstreetmap.domain.Codecs._

import cats.effect.{ConcurrentEffect, ContextShift}
import fs2.{Pipe, Stream}
import org.http4s.{Method, Request, Uri}
import kantan.xpath._
import kantan.xpath.implicits._
import scala.concurrent.ExecutionContext

class OpenStreetMapNeighborhood[F[_]: ConcurrentEffect: ContextShift] extends WithBlockingEcStream[F] {

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
      .filter(_.length > 0)
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
