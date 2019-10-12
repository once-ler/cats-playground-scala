package com.eztier.testhttp4sclient

import io.circe.generic.auto._
import cats.effect.{Bracket, Effect, ExitCode, IO, IOApp, Sync}
import fs2.Pipe
import org.http4s.{EntityBody, Header}
// import cats.effect._
import cats.implicits._
import io.chrisdavenport.vault.Vault
import io.circe.Json
import org.http4s.{EmptyBody, Headers, HttpVersion, Method, Request, Uri}
import org.http4s.client.Client
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder

import fs2.Stream
import fs2.text.utf8Encode

class Fetcher[F[_]: Sync] {
  def createRequest: Request[F] = Request[F](
    Method.GET,
    Uri.unsafeFromString("https://jsonplaceholder.typicode.com/users/10"),
    HttpVersion.`HTTP/1.1`, Headers.empty, EmptyBody, Vault.empty // The params on this line are optional.
  )

  def getSite(client: Client[F]) =  {

    val page = client.expect[Json](createRequest)

    page.map {
      a =>
        println(a.toString())
    }

  }
}

object Fetcher {
  def apply[F[_]: Sync] = new Fetcher()
}

class FakeSoap[F[_]: Sync] {
  def getBody(body: EntityBody[IO]): Array[Byte] = body.compile.toVector.unsafeRunSync.toArray

  def strBody(body: String): EntityBody[F] = fs2.Stream(body).through(utf8Encode)

  val headers = Headers.of(Header("Content-Type", "text/xml"), Header("SOAPAction", "https://www.w3schools.com/xml/FahrenheitToCelsius"))

  def createRequest: Request[F] = Request[F](
    method = Method.POST,
    uri = Uri.unsafeFromString("https://www.w3schools.com/xml/tempconvert.asmx"),
    headers = headers,
    body = strBody(
      """<?xml version="1.0" encoding="utf-8"?>
        |<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        |  <soap:Body>
        |    <FahrenheitToCelsius xmlns="https://www.w3schools.com/xml/">
        |      <Fahrenheit>75</Fahrenheit>
        |    </FahrenheitToCelsius>
        |  </soap:Body>
        |</soap:Envelope>
      """.stripMargin)
  )
  def getSite(client: Client[F]) = {
    val page = client.expect[String](createRequest)

    page.map{
      a =>
        println(a)
    }
  }
}

object FakeSoap {
  def apply[F[_]: Sync] = new FakeSoap()
}

// -----------------------------------------------------------------------------------------------
// Inifinite stream

object InfiniteClient {
  val ec = scala.concurrent.ExecutionContext.global
  implicit val cs = IO.contextShift(ec)
  
  val client = BlazeClientBuilder[IO](ec)

  case class Todo(userId: String, id: Int, title: String, completed: Boolean)

  def createRequest[F[_]: Effect]: Request[F] = Request[F](
    Method.GET,
    Uri.unsafeFromString("https://jsonplaceholder.typicode.com/todos"),
    HttpVersion.`HTTP/1.1`, Headers.empty, EmptyBody, Vault.empty // The params on this line are optional.
  )

  def clientStream[F[_]](dumbVar: Int = 0)(implicit F: Effect[F]): Pipe[F, Todo] =
    clientBodyStream(dumbVar) andThen byteStreamParserS andThen tweetPipeS andThen filterLeft

  def clientBodyStream[F[_]: Effect](dumbVar: Int): Pipe[F, TwitterUserAuthentication, Segment[Byte, Unit]] =
    taS =>
      for {
        ta <- taS
        client <- Http1Client.stream[F]()
        signedRequest <- Stream.repeatEval(F.pure(twitterStreamRequest[F](track))) // Endlessly Generate Requests
          .through(userSign(ta)) // Sign Them
        infiniteEntityBody <- client.streaming(signedRequest)(_.body.segments) // Transform to Efficient Segments
      } yield infiniteEntityBody

  def todoPipeS[F[_]]: Pipe[F, Json, Either[String, Todo]] = _.map{ json =>
    json.as[Todo].leftMap(pE => s"ParseError: ${pE.message} - ${json.printWith(Printer.noSpaces)}")
  }
}

object App extends IOApp {
  val ec = scala.concurrent.ExecutionContext.global

  val client = BlazeClientBuilder[IO](ec)

  def run(args: List[String]): IO[ExitCode] = client.resource.use(FakeSoap[IO].getSite).as(ExitCode.Success)


  def run2(args: List[String]): IO[ExitCode] = client.resource.use(Fetcher[IO].getSite).as(ExitCode.Success)
}
