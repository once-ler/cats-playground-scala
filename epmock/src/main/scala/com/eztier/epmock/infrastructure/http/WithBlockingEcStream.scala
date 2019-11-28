package com.eztier.epmock
package infrastructure.http

import java.util.concurrent.Executors

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, Sync}
import fs2.Stream
import fs2.text.{utf8DecodeC, utf8Encode}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityBody, Header, Headers, Request}

import scala.concurrent.ExecutionContext

abstract class WithBlockingEcStream[F[_]: ConcurrentEffect: ContextShift] {
  val headers = Headers.of(Header("Accept", "*/*"))

  def strBody(body: String): EntityBody[F] = fs2.Stream(body).through(utf8Encode)

  // Don't block the main thread
  private def blockingEcStream: Stream[F, ExecutionContext] =
    Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
      Sync[F].delay(pool.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  def createRequest(zipCode: String = ""): Request[F]

  def clientBodyStream(zipCode: String = ""): Stream[F, String] =
    blockingEcStream.flatMap {
      ec =>
        for {
          client <- BlazeClientBuilder[F](ec).stream
          plainRequest <- Stream.eval[F, Request[F]](Applicative[F].pure[Request[F]](createRequest(zipCode)))
          entityBody <- client.stream(plainRequest).flatMap(_.body.chunks).through(utf8DecodeC) // def utf8DecodeC[F[_]]: Pipe[F, Chunk[Byte], String]
        } yield entityBody
    }

}
