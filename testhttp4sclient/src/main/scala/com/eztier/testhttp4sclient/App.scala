package com.eztier.testhttp4sclient

import io.circe.generic.auto._
import cats.effect.{Bracket, ExitCode, IO, IOApp, Sync}
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

  val headers = Headers.of(Header("Content-Type", "text/xml"))

  def createRequest: Request[F] = Request[F](
    method = Method.POST,
    uri = Uri.unsafeFromString("https://www.w3schools.com/xml/tempconvert.asmx"),
    headers = headers,
    body = strBody(
      """
        |<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
        |  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        |  xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        |<soap:Body>
        |<FahrenheitToCelsius xmlns="http://www.w3schools.com/webservices/">
        |  <Fahrenheit>75</Fahrenheit>
        |</FahrenheitToCelsius>
        |</soap:Body>
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


object App extends IOApp {
  val ec = scala.concurrent.ExecutionContext.global

  def run(args: List[String]): IO[ExitCode] = BlazeClientBuilder[IO](ec).resource.use(FakeSoap[IO].getSite).as(ExitCode.Success)


  def run2(args: List[String]): IO[ExitCode] = BlazeClientBuilder[IO](ec).resource.use(Fetcher[IO].getSite).as(ExitCode.Success)
}
