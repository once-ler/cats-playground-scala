package com.eztier
package testhl7

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import java.util.concurrent.Executors

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.NodeSeq

object Package {

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

}

object Domain {
  case class SomeResponse
  (
    note: String
  )

  trait SomeResponseInvoke {
    def getSomeResponse(): Future[SomeResponse]
    def getBadResponse(): Future[SomeResponse]
  }
}

object Infrastructure {
  import Domain._

  implicit val singleThreadContext: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(3))

  class SomeResponseApi extends SomeResponseInvoke {
    override def getSomeResponse(): Future[SomeResponse] =
      Future(SomeResponse(note = "Done."))

    override def getBadResponse(): Future[SomeResponse] =
      Future.failed(new Exception("Sorry!"))
  }
}

object PlainFutureTest {
  import Domain._
  import Infrastructure._

  def run = {
    val f0 = new SomeResponseApi().getBadResponse
    f0.recover {
      case e: Exception =>
        println(e.getMessage)
        SomeResponse("Recovered from future error.")
    }

    // Cannot even get here.  Future executes immediately.
    val r0 = Await.result(f0, 2 seconds)

    println(r0)

  }
}

class AsyncTest[F[_]: Async] {
  import Package.blockingThreadPool
  import Domain._
  import Infrastructure._

  def run: F[SomeResponse] = {
    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, SomeResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            x <- new SomeResponseApi().getBadResponse
          } yield x

          f.recover {
            case e: Exception =>
              println(s"I survived from this real error: ${e.getMessage}") // Log it.
          }

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) =>
              cb(Right(SomeResponse("Really recovered from failure.")))
              // cb(Left(e)) // Fake a success.
          }
      }
    }
  }

}
