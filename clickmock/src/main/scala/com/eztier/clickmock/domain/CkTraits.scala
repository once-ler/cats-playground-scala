package com.eztier.clickmock
package domain

package com.eztier.clickmock.types

import java.util.Date

import cats.Applicative
import cats.effect.{Async, Bracket, IO, Sync}
import fs2.Pipe

import scala.xml.XML
// import org.apache.poi.ss.formula.functions.T
import scalikejdbc.{DBSession, NamedAutoSession, NamedDB}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.xml.{Elem, Node, NodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

import scalikejdbc.SQLSyntaxSupport

import com.eztier.clickmock.soap.ClickMockService
// import com.eztier.clickmock.implicits._

import fs2.Stream

trait CkBase {
  val oid: String
  val Class: String
  val extent: String
}

trait WithCustomAttributes {
  val customAttributes: EntityReference[_]
}

trait WithProject {
  val _webrUnique_ID: String
}

trait WithNonProject {
  val ID: String
}

trait WithFindById {
  def findById(id: String)(implicit session: DBSession): Option[_]
}

trait WithFindByMrn {
  def findByMrn(mrn: String)(implicit session: DBSession): Option[_]
}

trait WithExplicitTypeName {
  def typeName: String
}

trait WithEncoder {
  this: CkBase =>
  def encoder(name: String, el: Any) = {
    val in = el match {
      case a: String => <string value={a}></string>
      case a: Boolean => <boolean value={a.toString}></boolean>
      case a: Int => <integer value={a.toString}></integer>
      case a: Float => <float value={a.toString}></float>
      case a: Date => <date value={a.getTime.toString}></date>
      case a: EntityReference[_] if a.Poref.length > 0 => <entityReference type={a.Type} poref={a.Poref}></entityReference>
      case a: PersistentReference if a.Poref.length > 0 => <persistentReference poref={a.Poref}></persistentReference>
      case _ => <null />
    }
    <attr name={name}>{in}</attr>
  }

  def toXml = {
    val b = (Seq[NodeSeq]() /: this.getClass.getDeclaredFields.filter(f => !f.getName.matches("^\\b(?i)oid|class|extent\\b$"))) {
      (a, f) =>
        f.setAccessible(true)
        val x = encoder(if (f.getName == "_webrUnique_ID") "ID" else f.getName, f.get(this))

        a ++ x
    }
    <mainspan>{b}</mainspan>
  }

  def toCkTypeName = this.getClass.getSimpleName.replace("Ck", "")

  def toOid: String = this.oid
}

trait WithEntitySupport {
  // Get Entity
  def getEntity(oid: String) (implicit cf: ClickMockService[IO]): IO[NodeSeq] = {

    for {
      d <- cf.tryGetEntityByID(Some(oid))
      x <- cf.tryParseXML(d.getEntityByIDResult)
    } yield x

  }

  // Redefine Entity
  def redefineEntity(oid: String, xmlString: String) (implicit cf: ClickMockService[IO]): IO[NodeSeq] = {
    for {
      r <- cf.tryRedefineEntityByID(Some(oid), Some(xmlString))
      x <- cf.tryParseXML(r.redefineEntityByIDResult)
    } yield x
  }

  def redefineEntity[A <: CkBase with WithEncoder](in: A)(implicit cf: ClickMockService[IO]): IO[NodeSeq] =
    redefineEntity(in.Class + ":" + in.oid, in.toXml.toString)

  def redefineCompleteEntity[A <: CkBase with WithCustomAttributes with WithEncoder, B <: CkBase with WithEncoder](root: A, child: B)(implicit cf: ClickMockService[IO]): IO[NodeSeq] = {
    for {
      x <- redefineEntity(root.customAttributes.Poref, child.toXml.toString())
      y <- redefineEntity(root)
    } yield y
  }

  // Create Entity
  def createEntity(typeName: String, xmlString: String)(implicit cf: ClickMockService[IO]): IO[NodeSeq] = {
    for {
      r <- cf.tryCreateEntity(Some(typeName), Some(xmlString))
      x <- cf.tryParseXML(r.createEntityResult)
    } yield x
  }

  def createEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A)(implicit cf: ClickMockService[IO]): IO[NodeSeq] =
    createEntity(in.typeName, in.toXml.toString)

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

  def processInnerResultImpl[B <: WithEncoder](outer: B)(implicit cf: ClickMockService[IO]): NodeSeq => IO[NodeSeq] = (res: NodeSeq) => {
    val entity = res \ "entity"
    val typeName = (entity \ "@type").headOption.getOrElse(NodeSeq.Empty).text
    val poref = (entity \ "@poref").headOption.getOrElse(NodeSeq.Empty).text
    val xModified = outer updateCustomAttributes(typeName, poref)
    val xModifiedStr = xModified.toString

    for {
      r <- cf.tryCreateEntity(Some(outer.toCkTypeName), Some(xModifiedStr))
      x <- cf.tryParseXML(r.createEntityResult)
    } yield x
  }

  def processInnerResult[B <: WithEncoder](outer: B)(implicit cf: ClickMockService[IO]): Pipe[IO, NodeSeq, NodeSeq] = _.evalMap{
    res =>
      processInnerResultImpl(outer)(cf)(res)
  }

  def createCompleteEntity[A <: WithEncoder, B <: WithEncoder](inner: A, outer: B)(implicit cf: ClickMockService[IO]): IO[IO[NodeSeq]] = {
    for {
      r <- cf.tryCreateEntity(Some(inner.toCkTypeName), Some(inner.toXml.toString))
      x <- cf.tryParseXML(r.createEntityResult)
    } yield {
      processInnerResultImpl(outer)(cf)(x)
    }
  }

  /*
  def maybeMerge[A <: CkBase](fromCa: A, fromCk: A, fromCaCm: A) = {

    fromCa match {
      case a: CkPerson => fromCa.asInstanceOf[CkPerson].merge(fromCk).merge(fromCaCm)
      case a: Ck_Participant => fromCa.asInstanceOf[Ck_Participant].merge(fromCk).merge(fromCaCm)
      case a: Ck_ClickAddress => fromCa.asInstanceOf[Ck_ClickAddress].merge(fromCk).merge(fromCaCm)
      case a: Ck_ClickPartyContactInformation => fromCa.asInstanceOf[Ck_ClickPartyContactInformation].merge(fromCk).merge(fromCaCm)
      case a: Ck_PersonCustomExtension => fromCa.asInstanceOf[Ck_PersonCustomExtension].merge(fromCk).merge(fromCaCm)
      case a: Ck_ParticipantCustomExtension => fromCa.asInstanceOf[Ck_ParticipantCustomExtension].merge(fromCk).merge(fromCaCm)
      case _ => fromCa
    }
  }

  def maybeMerge[A <: CkBase](fromCaCm: A, fromCkCm: A) = {
    fromCaCm match {
      case a: CkPerson_CustomAttributesManager => fromCaCm.asInstanceOf[CkPerson_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_Participant_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_Participant_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_ParticipantRecord_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_ParticipantRecord_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_ClickAddress_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_ClickAddress_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_ClickPartyContactInformation_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_ClickPartyContactInformation_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_PersonCustomExtension_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_PersonCustomExtension_CustomAttributesManager].merge(fromCkCm)
      case a: Ck_ParticipantCustomExtension_CustomAttributesManager => fromCaCm.asInstanceOf[Ck_ParticipantCustomExtension_CustomAttributesManager].merge(fromCkCm)
      case _ => fromCaCm
    }
  }
  */
  
  /*
    mrn -> Empty means just create new
    fromCk -> Ck object from SQL database
    fromCa -> Cassandra Ca object implicitly transformed to Ck object
    fromCaCm -> Cassandra Ca object implicitly transformed to Ck Cm object
   */
  private def addOrUpdateImpl[A <: CkBase with WithCustomAttributes with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B) (implicit cf: ClickMockService[IO]): IO[NodeSeq] = {
    fromCk.oid match {
      case null | "" =>
        // Does root object with mrn exist?
        val a = if (mrn.length > 0) NamedDB('crms) readOnly { implicit session => fromCk.findById(mrn) } else None
        a match {
          case Some(c) =>
            val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
            val fromCa1 = maybeMerge(fromCa, c.asInstanceOf[A], fromCaCm1)
            cf.redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
          case _ =>
            // Create new
            cf.createCompleteEntity(fromCaCm, fromCa)
        }
      case _ =>
        // Object already exists, but may or may not have the mrn as its ID.
        val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
        val fromCa1 = maybeMerge(fromCa, fromCk, fromCaCm1)
        cf.redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
    }
  }

  def addOrUpdateNonProject[A <: CkBase with WithCustomAttributes with WithNonProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B)(implicit cf: ClickMockService): Future[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateProject[A <: CkBase with WithCustomAttributes with WithProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B)(implicit cf: ClickMockService): Future[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A)(implicit cf: ClickMockService): Future[NodeSeq] = {
    in.oid match {
      case null | "" => createEntity(in)
      case _ => redefineEntity(in)
    }
  }
}
