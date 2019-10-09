package com.eztier.testhttp4sclient

import io.circe.generic.auto._
import cats.effect.{Bracket, ExitCode, IO, IOApp, Sync}
// import cats.effect._
import cats.implicits._
import io.chrisdavenport.vault.Vault
import io.circe.Json
import org.http4s.{EmptyBody, Headers, HttpVersion, Method, Request, Uri}
import org.http4s.client.Client
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder

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

object App extends IOApp {
  val ec = scala.concurrent.ExecutionContext.global

  def run(args: List[String]): IO[ExitCode] = BlazeClientBuilder[IO](ec).resource.use(Fetcher[IO].getSite).as(ExitCode.Success)
}
