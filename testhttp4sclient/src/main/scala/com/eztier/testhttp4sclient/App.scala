package com.eztier.testhttp4sclient

import java.util.concurrent.Executors

import cats.{Applicative, Show}
import io.circe.generic.auto._
import cats.effect.{Bracket, Concurrent, ConcurrentEffect, ContextShift, Effect, ExitCode, IO, IOApp, Sync}
import fs2.Pipe
import io.circe.Printer
import org.http4s.{EntityBody, Header}
import org.typelevel.jawn.AsyncParser

import scala.concurrent.ExecutionContext
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

class InfiniteClient[F[_]: ConcurrentEffect: ContextShift] {
  import Util._

  // import io.circe.generic.auto._

  // jawn-fs2 needs to know what JSON AST you want
  import jawnfs2._

  // For .parseJsonStream()
  implicit val f = io.circe.jawn.CirceSupportParser.facade

  // For showLinesStdOut
  implicit val showTodo: Show[Todo] = Show.show(t => s"${t.id} ${t.title} ${t.userId}")

  // Don't block the main thread
  def blockingEcStream: Stream[F, ExecutionContext] =
    Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
      Sync[F].delay(pool.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  case class Todo(userId: Int, id: Int, title: String, completed: Boolean)

  def createRequest(dumbVar: Int = 0): Request[F] = Request[F](
    Method.GET,
    Uri.unsafeFromString("https://jsonplaceholder.typicode.com/todos"),
    HttpVersion.`HTTP/1.1`, Headers.empty, EmptyBody, Vault.empty // The params on this line are optional.
  )

  def clientStream(dumbVar: Int = 0, ec: ExecutionContext): Stream[F, Unit] =
    clientBodyStream(dumbVar, ec)
      .through(todoPipeS)
      .through(filterLeft) // Removes all errors
      .covary[F].showLinesStdOut


  def clientBodyStream(dumbVar: Int, ec: ExecutionContext): Stream[F, Json] =
    for {
      client <- BlazeClientBuilder[F](ec).stream
      plainRequest <- Stream.repeatEval[F, Request[F]](Applicative[F].pure[Request[F]](createRequest(dumbVar))) // Endlessly Generate Requests.  Will get a F[Request[F]].
      infiniteEntityBody <- client.stream(plainRequest).flatMap(_.body.chunks.parseJsonStream)
    } yield infiniteEntityBody


  def todoPipeS[F[_]]: Pipe[F, Json, Either[String, List[Todo]]] = _.map{ json =>
    json.as[List[Todo]].leftMap(pE => s"ParseError: ${pE.message} - ${json.printWith(Printer.noSpaces)}")
  }

  def run: F[Unit] =
    blockingEcStream.flatMap { blockingEc =>
      clientStream(23, blockingEc)
    }.compile.drain
}

object App extends IOApp {
  // Note: .as() is Functor
  // def as[B](b : B) : F[B]

  val ec = scala.concurrent.ExecutionContext.global

  val client = BlazeClientBuilder[IO](ec)

  def run3(args: List[String]): IO[ExitCode] = client.resource.use(FakeSoap[IO].getSite).as(ExitCode.Success)


  def run2(args: List[String]): IO[ExitCode] = client.resource.use(Fetcher[IO].getSite).as(ExitCode.Success)

  def run(args: List[String]): IO[ExitCode] = (new InfiniteClient[IO]).run.as(ExitCode.Success)

}
