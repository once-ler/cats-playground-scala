package com.eztier
package testhl7.tagless.multi
package Ep

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{NodeSeq, XML}
import cats.implicits._
import cats.data.{Chain, EitherT}
import cats.{Applicative, Monad}
import cats.effect.{Async, Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import algae._
import algae.mtl.MonadLog
import io.chrisdavenport.log4cats.Logger
import common.CatsLogger._
import Ck.Domain._

object Package {
  import Domain._

  def createEpPatientAggregatorResource[F[_]: Async :ContextShift :ConcurrentEffect: Timer] =
    for {
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(createMonadLog[F, Chain[String]])
      epPatientAggregator = new EpPatientAggregator[F]
    } yield epPatientAggregator
}

object Domain {
  class EpPatientAggregator[F[_]: Applicative: Async: Concurrent](implicit logs: MonadLog[F, Chain[String]]) {

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
