package com.eztier
package testhl7.tagless

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{NodeSeq, XML}

import cats.implicits._
import cats.data.{Chain, EitherT}
import cats.{Applicative, Monad}
import cats.effect.{Async, Concurrent, Resource, Sync}

import algae._
import algae.mtl.MonadLog

import io.chrisdavenport.log4cats.Logger

import common.CatsLogger._

object Package {

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newFixedThreadPool(3)
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

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
    def getEntityF(oid: Option[String])(implicit logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]]
  }

  class CkEntityService[F[_]](repo: CkEntityAlgebra[F]) {
    def getEntity(oid: Option[String]): F[NodeSeq] =
      repo.getEntity(oid)

    def getEntityF(oid: Option[String])(implicit logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]] =
      repo.getEntityF(oid)
  }

  class CkEntityAggregator[F[_]: Applicative: Async: Concurrent](entityService: CkEntityService[F]) {
    def getOrCreate(oid: Option[String]): F[NodeSeq] =
      entityService.getEntity(oid)

    def getOrCreateF(oid: Option[String])(implicit logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]] =
      entityService.getEntityF(oid)
  }

  class EpPatientAggregator[F[_]: Applicative: Async: Concurrent](logs: MonadLog[F, Chain[String]]) {

    implicit val logging = logs

    def getOrCreateEntity(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]): F[CkParticipantAggregate] =
      for {
        x0 <- ckEntityAggregator.getOrCreate(oid)
        a: GetEntityByIDResponse = WrappedEntityXml(x0)
        x1 <- ckEntityAggregator.getOrCreate(oid)
        b: GetEntityByIDResponse = WrappedEntityXml(x1)
        x2 <- ckEntityAggregator.getOrCreate(oid)
        c: GetEntityByIDResponse = WrappedEntityXml(x2)
        x3 <- ckEntityAggregator.getOrCreate(oid)
        d: GetEntityByIDResponse = WrappedEntityXml(x3)
      } yield CkParticipantAggregate(
        entityA = a.some,
        entityB = b.some,
        entityC = c.some,
        entityD = d.some
      )

    def getOrCreateEntityF(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]): F[CkParticipantAggregate] =
      for {
        x0 <- ckEntityAggregator.getOrCreateF(oid)
        a: Option[GetEntityByIDResponse] = x0 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x1 <- ckEntityAggregator.getOrCreateF(oid)
        b: Option[GetEntityByIDResponse] = x1 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x2 <- ckEntityAggregator.getOrCreateF(oid)
        c: Option[GetEntityByIDResponse] = x2 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x3 <- ckEntityAggregator.getOrCreateF(oid)
        d: Option[GetEntityByIDResponse] = x3 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        l <- logs.get
        _ <- Logger[F].error(l.show) // Write out all the accumulated errors.
      } yield CkParticipantAggregate(
          entityA = a,
          entityB = b,
          entityC = c,
          entityD = d
        )
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

  class CkEntityInterpreter[F[_]: Async](cf: CkMockService[F])
    extends CkEntityAlgebra[F] {

    import Domain._

    val logger = implicitly[Logger[F]]

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


    override def getEntityF(oid: Option[String])(implicit logs: MonadLog[F, Chain[String]]): F[Either[String, NodeSeq]] =
      Sync[F].suspend(cf.tryGetEntityByID(oid))
        .attempt
        .flatMap {
          case Left(e) =>
            val a: F[Option[GetEntityByIDResponse]] = for {
              // _ <- logger.error(e.getMessage) // Writeout to disk immediately or allow to accumulate using MonadLog below.
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

      /*
      // Original implementation without logging.

      Monad[F].flatMap {
        // handleError from cats.syntax.ApplicativeErrorOps
        cf.tryGetEntityByID(oid).handleError{ e => None }
      } { d =>
        val e: EitherT[F, String, NodeSeq] = d match {
          case Some(x) => EitherT.right(cf.tryParseXML(x.getEntityByIDResult))
          case _ => EitherT.leftT("")
        }
        e.value
      }
      */

  }
}
