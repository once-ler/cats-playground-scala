package com.eztier
package testhl7.tagless

import java.util.concurrent.Executors

import cats.implicits._
import cats.{Applicative, Monad}
import cats.effect.{Async, Concurrent, Resource, Sync}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{NodeSeq, XML}

object Package {

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

}

object Domain {

  case class GetEntityByIDResponse
  (
    getEntityByIDResult: Option[String]
  )

  trait CkEntityAlgebra[F[_]] {
    def getEntity(oid: Option[String]): F[NodeSeq]
  }

  class CkEntityService[F[_]](repo: CkEntityAlgebra[F]) {
    def getEntity(oid: Option[String]): F[NodeSeq] =
      repo.getEntity(oid)
  }

  class CkEntityAggregator[F[_]: Applicative: Async: Concurrent](entityService: CkEntityService[F]) {
    def getOrCreate(oid: Option[String]): F[NodeSeq] =
      entityService.getEntity(oid)
  }

  class EpPatientAggregator[F[_]: Applicative: Async: Concurrent] {
    def getOrCreateEntity(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]) = {
      ckEntityAggregator.getOrCreate(oid)
    }
  }

}

object Infrastructure {
  import Domain._
  import Package._

  // Wrapper
  class CkMockService[F[_]: Async] {

    def tryGetEntityByID(oid: Option[String]): F[GetEntityByIDResponse] = {
      blockingThreadPool.use { ec: ExecutionContext =>

        Async[F].async {
          (cb: Either[Throwable, GetEntityByIDResponse] => Unit) =>

            implicit val ecc = implicitly[ExecutionContext](ec)

            val f: Future[GetEntityByIDResponse] = Future.failed(new Exception("Sorry!"))

            f.onComplete {
              case Success(s) => cb(Right(s))
              case Failure(e) => cb(Left(e))
            }
        }
      }
    }

    def tryParseXML(xmlStr: Option[String]): F[NodeSeq] = Applicative[F].pure(XML.loadString(xmlStr getOrElse "<result />"))

  }

  class CkEntityInterpreter[F[_]: Async](cf: CkMockService[F])
    extends CkEntityAlgebra[F] {

    override def getEntity(oid: Option[String]): F[NodeSeq] = {
      Monad[F].flatMap(cf.tryGetEntityByID(oid))(d => cf.tryParseXML(d.getEntityByIDResult))
    }

  }
}
