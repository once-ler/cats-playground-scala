package com.eztier
package testhl7.tagless.multi
package Ck

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{NodeSeq, XML}
import cats.implicits._
import cats.data.{Chain, EitherT}
import cats.{Applicative, Monad}
import cats.effect.{Async, Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
// import algae._
// import algae.mtl.MonadLog
import com.eztier.common.MonadLog

import io.chrisdavenport.log4cats.Logger
import common.CatsLogger._

object Package {

  import Domain._
  import Infrastructure._

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newFixedThreadPool(3)
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createCkAggregatorResource[F[_]: Async :ContextShift :ConcurrentEffect: Timer] = {
    for {
      // implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(createMonadLog[F, Chain[String]])
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(MonadLog.createMonadLog[F, String])
      ms = new CkMockService
      entityRepo = new CkEntityInterpreter[F](ms)
      entityService = new CkEntityService(entityRepo)
      entityAggregator = new CkEntityAggregator(entityService)
    } yield entityAggregator
  }

}

object Domain {

  case class GetEntityByIDResponse
  (
    getEntityByIDResult: Option[String] = None
  )

  case class CkParticipantAggregate
  (
    entityA: Option[GetEntityByIDResponse] = None,
    entityB: Option[GetEntityByIDResponse] = None,
    entityC: Option[GetEntityByIDResponse] = None,
    entityD: Option[GetEntityByIDResponse] = None
  )

  case class WrappedEntityXml(xml: NodeSeq)

  // Simple implicit from WrappedEntityXml => GetEntityByIDResponse
  implicit def fromEntityXmlToGetEntityByIDResponse(in: WrappedEntityXml): GetEntityByIDResponse =
    GetEntityByIDResponse()

  trait CkEntityAlgebra[F[_]] {
    def getEntity(oid: Option[String]): F[NodeSeq]

    // def getEntityF(oid: Option[String], logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]]
    def getEntityF(oid: Option[String]): F[Either[String, NodeSeq]]
  }

  class CkEntityService[F[_]](repo: CkEntityAlgebra[F]) {
    def getEntity(oid: Option[String]): F[NodeSeq] =
      repo.getEntity(oid)

    def getEntityF(oid: Option[String]): F[Either[String, NodeSeq]] =
      repo.getEntityF(oid)
  }

  class CkEntityAggregator[F[_] : Applicative : Async : Concurrent : MonadLog[?[_], Chain[String]]](entityService: CkEntityService[F]) {
    val logs = implicitly[MonadLog[F, Chain[String]]]

    def getOrCreate(oid: Option[String]): F[NodeSeq] =
      entityService.getEntity(oid)

    def getOrCreateF(oid: Option[String]): F[Either[String, NodeSeq]] =
      entityService.getEntityF(oid)

    def getMonadLogStorage: F[Chain[String]] = {
      for {
        x <- logs.get
      } yield x
    }
  }

}

object Infrastructure {
  import Domain._
  import Package._

  // Wrapper for some async effect, probably accessing a REST endpoint or database.
  class CkMockService[F[_]: Async] {

    def tryGetEntityByID(oid: Option[String]): F[Option[GetEntityByIDResponse]] = {
      blockingThreadPool.use { ec: ExecutionContext =>

        Async[F].async {
          (cb: Either[Throwable, Option[GetEntityByIDResponse]] => Unit) =>

            implicit val ecc = implicitly[ExecutionContext](ec)

            val f: Future[GetEntityByIDResponse] = Future.failed(new Exception("Sorry!"))

            f.onComplete {
              case Success(s) => cb(Right(s.some))
              case Failure(e) => cb(Left(e))
            }
        }
      }
    }

    def tryParseXML(xmlStr: Option[String]): F[NodeSeq] =
      Applicative[F].pure(XML.loadString(xmlStr getOrElse "<result />"))

  }

  class CkEntityInterpreter[F[_]: Async : MonadLog[?[_], Chain[String]]](cf: CkMockService[F])
    extends CkEntityAlgebra[F] {

    import Domain._

    val logger = implicitly[Logger[F]]
    val logs = implicitly[MonadLog[F, Chain[String]]]

    override def getEntity(oid: Option[String]): F[NodeSeq] =
      Monad[F].flatMap{
        // handleError from cats.syntax.ApplicativeErrorOps
        cf.tryGetEntityByID(oid)
          .handleError{
            e =>
              println(e.getMessage)
              Some(GetEntityByIDResponse())
          }
      } (d => cf.tryParseXML(d.get.getEntityByIDResult))


    override def getEntityF(oid: Option[String]): F[Either[String, NodeSeq]] =
      Sync[F].suspend(cf.tryGetEntityByID(oid))
        .attempt
        .flatMap {
          case Left(e) =>
            val a: F[Option[GetEntityByIDResponse]] = for {
              _ <- logs.log(Chain(e.getMessage))
              y = None
            } yield y
            a
          case Right(x) => x.pure[F]
        }
        .flatMap {
          d =>
            val e: EitherT[F, String, NodeSeq] = d match {
              case Some(x) => EitherT.right(cf.tryParseXML(x.getEntityByIDResult))
              case _ => EitherT.leftT("")
            }
            e.value
        }
  }
}