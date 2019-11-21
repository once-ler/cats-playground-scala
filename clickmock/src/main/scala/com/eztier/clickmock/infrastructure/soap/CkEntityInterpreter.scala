package com.eztier.clickmock
package infrastructure.soap

import cats.{Applicative, Monad}
import cats.effect.Async
import fs2.Pipe
import scala.xml.{Elem, Node, NodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

import domain._
import CkMergeTypeImplicits._

class CkEntityInterpreter[F[_]: Async] {
  // Get Entity
  def getEntity(oid: String) (implicit cf: CkMockService[F]): F[NodeSeq] =
    Monad[F].flatMap(cf.tryGetEntityByID(Some(oid)))(d => cf.tryParseXML(d.getEntityByIDResult))

  // Redefine Entity
  def redefineEntity(oid: String, xmlString: String) (implicit cf: CkMockService[F]): F[NodeSeq] =
    Monad[F].flatMap(cf.tryRedefineEntityByID(Some(oid), Some(xmlString)))(d => cf.tryParseXML(d.redefineEntityByIDResult))

  def redefineEntity[A <: CkBase with WithEncoder](in: A)(implicit cf: CkMockService[F]): F[NodeSeq] =
    redefineEntity(in.Class + ":" + in.oid, in.toXml.toString)

  def redefineCompleteEntity[A <: CkBase with WithCustomAttributes with WithEncoder, B <: CkBase with WithEncoder](root: A, child: B)(implicit cf: CkMockService[F]): F[NodeSeq] =
    if (root.customAttributes != None) Monad[F].flatMap(redefineEntity(root.customAttributes.get.Poref.getOrElse(""), child.toXml.toString()))(a => redefineEntity(root)) else Applicative[F].pure(NodeSeq.Empty)

  // Create Entity
  def createEntity(typeName: String, xmlString: String)(implicit cf: CkMockService[F]): F[NodeSeq] =
    Monad[F].flatMap(cf.tryCreateEntity(Some(typeName), Some(xmlString)))(d => cf.tryParseXML(d.createEntityResult))

  def createEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A)(implicit cf: CkMockService[F]): F[NodeSeq] =
    createEntity(in.typeName.getOrElse(""), in.toXml.toString)

  implicit class WrapWithEncoder[B <: WithEncoder](outer: B) {
    // Updates the outer entity with new reference to customAttributes entity
    def updateCustomAttributes(typeName: String, poref: String) = {
      val node = outer.toXml
      val updateNode = new RewriteRule() {
        override def transform(node: Node) = node match {
          case e@Elem(_, "attr", _, _, child@_*) if (e \ "@name").text == "customAttributes" => <attr name="customAttributes">
            <entityReference type={typeName} poref={poref}></entityReference>
          </attr>
          case other => other
        }
      }

      new RuleTransformer(updateNode).transform(node)
    }
  }

  def processInnerResultImpl[B <: WithEncoder](outer: B)(implicit cf: CkMockService[F]): NodeSeq => F[NodeSeq] = (res: NodeSeq) => {
    val entity = res \ "entity"
    val typeName = (entity \ "@type").headOption.getOrElse(NodeSeq.Empty).text
    val poref = (entity \ "@poref").headOption.getOrElse(NodeSeq.Empty).text
    val xModified = outer updateCustomAttributes(typeName, poref)
    val xModifiedStr = xModified.toString

    Monad[F].flatMap(cf.tryCreateEntity(Some(outer.toCkTypeName), Some(xModifiedStr)))(d => cf.tryParseXML(d.createEntityResult))
  }

  def processInnerResult[B <: WithEncoder](outer: B)(implicit cf: CkMockService[F]): Pipe[F, NodeSeq, NodeSeq] =
    _.evalMap(res => processInnerResultImpl(outer)(cf)(res))

  def createCompleteEntity[A <: WithEncoder, B <: WithEncoder](inner: A, outer: B)(implicit cf: CkMockService[F]): F[NodeSeq] = {
    val fa = Monad[F].flatMap(cf.tryCreateEntity(Some(inner.toCkTypeName), Some(inner.toXml.toString)))(d => cf.tryParseXML(d.createEntityResult))
    Monad[F].flatMap(fa)(x => processInnerResultImpl(outer)(cf)(x))
  }

  /*
    mrn -> Empty means just create new
    fromCk -> Ck object from SQL database
    fromCa -> Cassandra Ca object implicitly transformed to Ck object
    fromCaCm -> Cassandra Ca object implicitly transformed to Ck Cm object
   */
  private def addOrUpdateImpl[A <: CkBase with WithCustomAttributes with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B) (implicit cf: CkMockService[F]): F[NodeSeq] = {
    fromCk.oid match {
      case null | Some("") =>
        // Does root object with mrn exist?
        val a = if (mrn.length > 0) fromCk.findById(Some(mrn)) else None
        a match {
          case Some(c) =>
            val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
            val fromCa1 = maybeMerge(fromCa, c.asInstanceOf[A], fromCaCm1)
            redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
          case _ =>
            // Create new
            createCompleteEntity(fromCaCm, fromCa)
        }
      case _ =>
        // Object already exists, but may or may not have the mrn as its ID.
        val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
        val fromCa1 = maybeMerge(fromCa, fromCk, fromCaCm1)
        redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
    }
  }

  def addOrUpdateNonProject[A <: CkBase with WithCustomAttributes with WithNonProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B)(implicit cf: CkMockService[F]): F[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateProject[A <: CkBase with WithCustomAttributes with WithProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B)(implicit cf: CkMockService[F]): F[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A)(implicit cf: CkMockService[F]): F[NodeSeq] = {
    in.oid match {
      case null | Some("") => createEntity(in)
      case _ => redefineEntity(in)
    }
  }

  def addOrCreateEntityFlow[A <: CkBase with WithEncoder with WithExplicitTypeName](implicit cf: CkMockService[F]): Pipe[F, A, NodeSeq] = _.evalMap {
    a =>
      a match {
        case a if a.getClass == classOf[CkPostalContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkPostalContactInformation])
        case a if a.getClass == classOf[CkEmailContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkEmailContactInformation])
        case a if a.getClass == classOf[CkPhoneContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkPhoneContactInformation])
        case _ => Applicative[F].pure(NodeSeq.Empty)
      }
  }

}

object CkEntityInterpreter {
  def apply[F[_]: Async]: CkEntityInterpreter[F] = new CkEntityInterpreter()
}