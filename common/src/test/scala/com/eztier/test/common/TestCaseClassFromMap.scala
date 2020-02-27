package com.eztier
package test.common

import cats.syntax.option._
import shapeless._
import record._
import syntax.singleton._
import java.time.Instant

import org.specs2.mutable._

import scala.xml.NodeSeq

import common._

case class EntityReference[A <: Any](Poref: Option[String] = None, Type: Option[String] = None)

trait CkBase {
  val oid: Option[String]
  val Class: Option[String]
  val extent: Option[String]
}

case class CkCompany
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  name: Option[String] = None,
) extends CkBase

case class Ck_GenderSelection_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  enabled: Option[Int] = Some(1)
) extends CkBase

case class Ck_GenderSelection
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_GenderSelection_CustomAttributesManager]] = Some(EntityReference[Ck_GenderSelection_CustomAttributesManager]())
) extends CkBase


case class Ck_PersonCustomExtension_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  gender: Option[EntityReference[Ck_GenderSelection]] = Some(EntityReference[Ck_GenderSelection]())
) extends CkBase


case class Ck_PersonCustomExtension(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]] = Some(EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]())
) extends CkBase


case class CkPerson_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  personCustomExtension: Option[EntityReference[Ck_PersonCustomExtension]] = Some(EntityReference[Ck_PersonCustomExtension]())
) extends CkBase

case class CkPerson(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  employer: Option[EntityReference[CkCompany]] = Some(EntityReference[CkCompany]()),
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  middleName: Option[String] = None,
  customAttributes: Option[EntityReference[CkPerson_CustomAttributesManager]] = Some(EntityReference[CkPerson_CustomAttributesManager]()),
  dateOfBirth: Option[Instant] = None,
  gender: Option[String] = Some("Male")
) extends CkBase


class TestCaseClassFromMap extends Specification {

  private def parseEntity(root: NodeSeq): Map[String, Any] = {
    val entity = root \ "entity"
    val r = entity \ "attr"
    val m = (Map[String, Any]() /: r) {
      (a, n) =>
        val h = n.child.head

        val e = h.label match {
          case "entityreference" | "persistentReference" => Map("poref" -> h.attribute("poref").getOrElse("").toString, "type" -> h.attribute("type").getOrElse("").toString)
          case "date" => Instant.ofEpochMilli(h.attribute("value").getOrElse("0").toString.toLong) // new Date(h.attribute("value").getOrElse("0").toString.toLong)
          case _ => h.attribute("value").getOrElse("").toString
        }

        a + (n.attribute("name").getOrElse("").toString -> e.some)
    }

    val poref = (entity \ "@poref").headOption.getOrElse("").toString
    val pattern = "(.+):(.+)".r
    val pattern(clz, oid) = poref

    m ++ Map(
      "oid" -> oid.some,
      "Class" -> clz.some,
      "type" -> (entity \ "@type").headOption.getOrElse("").toString.some
    )
  }

  "" should {
    "" in {

      val xml = <mainspan><entity poref="class:1234" type="Person"><attr name="firstName"><string value="Foo"></string></attr></entity></mainspan>

      val m = parseEntity(xml)

      val c = CaseClassFromMap[CkPerson](m)

      1 mustEqual 1
    }
  }


}
