package com.eztier.datasource
package infrastructure.http

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.{ConcurrentEffect, Sync}
import fs2.Stream

trait WithBlockingStream {
  def blockingEcStream[F[_]: ConcurrentEffect]: Stream[F, ExecutionContext] =
    Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
      Sync[F].delay(pool.shutdown()))
      .map(ExecutionContext.fromExecutorService)
}