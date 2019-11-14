package com.eztier.clickmock
package test

import java.util.concurrent.Executors
import cats.Applicative

/*
  def blockingThreadPool(implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })
  */

class TestClickMockSpec {

}
