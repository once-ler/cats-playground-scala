package com.eztier.clickmock

import java.net.URI
import java.util.concurrent.Executors

import cats.effect.{Async, Concurrent, ContextShift, Resource, Sync}
import soap._
import config._

import scala.concurrent.{ExecutionContext, Future}


class ClickMockService[F[_]: Sync : Async : ContextShift : Concurrent](conf: AppConfig) {

  def blockingThreadPool(implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  val entityservice = new EntityServicesSoap12Bindings with
    scalaxb.SoapClientsAsync with
    scalaxb.DispatchHttpClientsAsync {
    override def baseAddress: URI = conf.soap.url.fold(super.baseAddress)(a => new java.net.URI(a))
  }.service

  val esetservice = new EntitySetServicesSoap12Bindings with
    scalaxb.SoapClientsAsync with
    scalaxb.DispatchHttpClientsAsync {
    override def baseAddress: URI = conf.soap.url.fold(super.baseAddress)(a => new java.net.URI(a))
  }.service

  def tryGetEntityByID(oidStr: Option[String]): Future[GetEntityByIDResponse] = {
    blockingThreadPool.use {
      ec: ExecutionContext =>

      val ecc = implicitly[ExecutionContext](ec)

      val f = for {
        l <- entityservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
        a <- entityservice.getEntityByID(l.LoginResult, oidStr)
          .recover {
            case x: scalaxb.Fault[Any] =>
            // val err = s"getEntityByID() oidStr: ${Some(oidStr)} " + x.toString
            // logger.error(err)
            // new GetEntityByIDResponse()
            // throw new RuntimeException(err)
            case _ =>
              new GetEntityByIDResponse()
          }
        z <- entityservice.logoff(l.LoginResult)
      } yield a

      ContextShift[F].evalOn(ec) {
        Async[F].async {
          cb =>



        }
      }


    }
  }
}