package com.eztier.clickmock
package infrastructure

import java.net.URI
import java.util.Date

import cats.syntax.applicative._
import cats.Applicative
import cats.effect.{Async, Concurrent, ContextShift, Resource, Sync}
import fs2.Pipe
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.xml.{Node, NodeSeq, XML}

import soap._
import config._
import domain._
import CkXmlToTypeImplicits._

class CkMockService[F[_]: Async](conf: AppConfig, blockingThreadPool: Resource[F, ExecutionContext]) {

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

  def tryGetEntityByID(oidStr: Option[String]): F[GetEntityByIDResponse] = {

    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, GetEntityByIDResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            l <- entityservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
            a <- entityservice.getEntityByID(l.LoginResult, oidStr)
            z <- entityservice.logoff(l.LoginResult)
          } yield a

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) => cb(Left(e))
          }
      }
    }
  }

  def tryGetEntitySetElements(oidStr: Option[String]): F[GetEntitySetElementsResponse] = {
    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, GetEntitySetElementsResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            l <- esetservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
            a <- esetservice.getEntitySetElements(l.LoginResult, oidStr, false)
            z <- esetservice.logoff(l.LoginResult)
          } yield a

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) => cb(Left(e))
          }
      }
    }
  }

  def tryCreateEntity(entityType: Option[String], xmlString: Option[String]): F[CreateEntityResponse] = {
    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, CreateEntityResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            l <- entityservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
            a <- entityservice.createEntity(l.LoginResult, entityType, xmlString)
            z <- entityservice.logoff(l.LoginResult)
          } yield a

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) => cb(Left(e))
          }
      }
    }
  }

  def tryRedefineEntityByID(oidStr: Option[String], xmlString: Option[String]): F[RedefineEntityByIDResponse] = {
    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, RedefineEntityByIDResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            l <- entityservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
            a <- entityservice.redefineEntityByID(l.LoginResult, oidStr, xmlString)
            z <- entityservice.logoff(l.LoginResult)
          } yield a

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) => cb(Left(e))
          }
      }
    }
  }

  def tryUnregisterEntityByID(oidStr: Option[String]): F[UnregisterEntityByIDResponse] = {
    blockingThreadPool.use { ec: ExecutionContext =>

      Async[F].async {
        (cb: Either[Throwable, UnregisterEntityByIDResponse] => Unit) =>

          implicit val ecc = implicitly[ExecutionContext](ec)

          val f = for {
            l <- entityservice.login(conf.soap.store, conf.soap.user, conf.soap.pass)
            a <- entityservice.unregisterEntityByID(l.LoginResult, oidStr)
            z <- entityservice.logoff(l.LoginResult)
          } yield a

          f.onComplete {
            case Success(s) => cb(Right(s))
            case Failure(e) => cb(Left(e))
          }
      }
    }
  }

  def attributeValueEquals(key: String, value: String)(node: Node) = {
    node.attributes.exists(_.key == key) && node.attributes.exists(_.value.text == value)
  }

  def attributeKeyEquals(key: String)(node: Node) = node.attributes.exists(_.key == key)

  def tryParseXML(xmlStr: Option[String]): F[NodeSeq] = Applicative[F].pure(XML.loadString(xmlStr getOrElse "<mainspan />"))

  def tryGetAttrText(root: NodeSeq, key: String, value: String): F[String] = {
    val r = root \ "entity" \ "attr" filter attributeValueEquals(key, value)
    Applicative[F].pure(r.text)
  }

  def tryGetAttrString(root: NodeSeq, key: String, value: String): F[String] = {
    val r = root \ "entity" \ "attr" filter attributeValueEquals(key, value)
    val p = r \ "string"
    val h = p.headOption

    val z = for { x <- h; v <- x.head.attribute("value") }
      yield v.toString

    Applicative[F].pure(z.getOrElse(""))
  }

  def tryGetAttrDate(root: NodeSeq, key: String, value: String): F[Option[Date]] = {
    val r = root \ "entity" \ "attr" filter attributeValueEquals(key, value)
    val p = r \ "date"
    val h = p.headOption

    val z = for { x <- h; v <- x.head.attribute("value") }
      yield new Date(v.toString.toLong)

    Applicative[F].pure(z)
  }

  // entityreference | persistentReference
  def tryGetAttrRefMap(root: NodeSeq, key: String, value: String, refType: String): F[Map[String, String]] = {
    val r = root \ "entity" \ "attr" filter attributeValueEquals(key, value)
    val p = r \ refType // filter attributeKeyEquals("poref")
    val a = Map("text" -> r.text)

    val h = p.headOption
    val m = h match {
      case Some(h) => Map("poref" -> p.head.attribute("poref").getOrElse("").toString, "type" -> p.head.attribute("type").getOrElse("").toString)
      case _ => Map("poref" -> "", "type" -> "")
    }

    val b = a ++ m

    Applicative[F].pure(b)
  }

  // entityreference | persistentReference
  def tryGetAttrRef(root: NodeSeq, key: String, value: String, refType: String): F[String] = {
    val r = root \ "entity" \ "attr" filter attributeValueEquals(key, value)
    val p = r \ refType // filter attributeKeyEquals("poref")

    val h = p.headOption
    h match {
      case Some(h) => Applicative[F].pure(p.head.attribute("poref").getOrElse("") toString)
      case _ => Applicative[F].pure("")
    }
  }

  def tryGetPoref(root: NodeSeq): Future[String] = Future((root \ "entity" \ "@poref").headOption.getOrElse("").toString)

  def getEntityPoref(root: NodeSeq): String = (root \ "entity" \ "@poref").headOption.getOrElse("").toString

  def getEntityType(root: NodeSeq): String = (root \ "entity" \ "@type").headOption.getOrElse("").toString

  val getIdFromEntityXmlFlow: Pipe[F, NodeSeq, String] = _.evalMap{ tryGetAttrString(_, "name", "ID") }

  def fromEntityXmlToCkTypeFlow: Pipe[F, NodeSeq, Option[CkBase]] = _.evalMap {
    x =>
      val a = getEntityType(x) match {
        case a if a == "Postal Contact Information" =>
          val b: CkPostalContactInformation = WrappedEntityXml(xml = x)
          Some(b)
        case a if a == "E-mail Contact Information" =>
          val b: CkEmailContactInformation = WrappedEntityXml(xml = x)
          Some(b)
        case a if a == "Phone Contact Information" =>
          val b: CkPhoneContactInformation = WrappedEntityXml(xml = x)
          Some(b)
        case _ => None
      }

      a.pure[F]
  }
}

object CkMockService {
  def apply[F[_]: Sync : Async : ContextShift : Concurrent](conf: AppConfig, blockingThreadPool: Resource[F, ExecutionContext]) =
    new CkMockService[F](conf, blockingThreadPool)
}