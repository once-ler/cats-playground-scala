package com.eztier
package datasource
package infrastructure.http

import java.util.concurrent.Executors

import cats.data.EitherT
import fs2.Stream
import fs2.text.{utf8DecodeC, utf8Encode}
import cats.implicits._
import cats.effect.{ConcurrentEffect, IO}
import org.http4s.{Charset, EntityBody, Method, Request, Uri, UrlForm}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.Status.Successful

import scala.concurrent.ExecutionContext
import common.Util._

import scala.util.{Failure, Success, Try}

class HttpSession[F[_]: ConcurrentEffect] extends WithBlockingStream {
  private val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
  private val client = BlazeClientBuilder(ec)

  private def getBody(body: EntityBody[IO]): Array[Byte] = body.compile.toVector.unsafeRunSync.toArray

  private def strBody(body: String): EntityBody[F] = Stream(body).through(utf8Encode)

  def postWithBody(uri: String, body: String): EitherT[F, String, String] = {
    val req = Request[F](
      method = Method.POST,
      uri = Uri.unsafeFromString(uri),
      body = strBody(body)
    )

    Try {
      client.resource.use(_.expectOr[String](req)(r => {
        // On failure, the status code is returned.  The underlying HTTP connection is closed at the completion of the decoding.
        throw new Throwable(s"Request failed.  Status code: ${r.status.code.toString}")
        // s"Request failed.  Status code: ${r.status.code.toString}".pure[F]
      }))
    } match {
      case Success(a) => EitherT.right(a)
      case Failure(f) => EitherT.leftT(f.getMessage)
    }
  }

  def getF = {
    val req = Request[F](
      method = Method.POST,
      uri = Uri.unsafeFromString("https://duckduckgo.com/")
    ).withEntity(UrlForm("q" -> "http4s"))(UrlForm.entityEncoder(Charset.`UTF-8`))

    client.resource.use(a => a.fetch[String](req) {
      case Successful(a) => a.as[String]
      case r => s"Request failed.  Status code: ${r.status.code.toString}".pure[F]
    })
/*
    client.resource.use(_.expectOr[String](req)(r => {
      // On failure, the status code is returned.  The underlying HTTP connection is closed at the completion of the decoding.
      throw new Throwable(s"Request failed.  Status code: ${r.status.code.toString}")
      // s"Request failed.  Status code: ${r.status.code.toString}".pure[F]
    }))
*/
  }

  def getStream = {
    val req = Request[F](
      method = Method.POST,
      uri = Uri.unsafeFromString("https://duckduckgo.com/")
    ).withEntity(UrlForm("q" -> "http4s"))(UrlForm.entityEncoder(Charset.`UTF-8`))

    for {
      s <- client.stream
      r <- s.stream(req).flatMap(_.body.chunks).through(utf8DecodeC).handleErrorWith {
        e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.emit(ex)
      }
    } yield r
  }
}

object HttpSession {
  def apply[F[_]: ConcurrentEffect](): HttpSession[F] = new HttpSession[F]
}
