package com.eztier
package test.common

import cats.syntax.option._

// import shapeless._
// import record._
// import syntax.singleton._

import java.time.Instant

import org.specs2.mutable._

import scala.xml.NodeSeq

import common._

case class EntityReference[A <: Any](Poref: Option[String] = None, Type: Option[String] = None)
case class PersistentReference(Poref: Option[String] = None)

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

        val h = n.child.filter(_.label != "#PCDATA").head

        val e = h.label match {
          case "entityreference" => Map("Poref" -> h.attribute("poref").getOrElse("").toString.some, "Type" -> h.attribute("type").getOrElse("").toString.some)
          case "persistentReference" =>  Map("Poref" -> h.attribute("poref").getOrElse("").toString.some)
          case "date" => Instant.ofEpochMilli(h.attribute("value").getOrElse("0").toString.toLong)
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

  import shapeless.Lazy
  import io.circe.Decoder
  import io.circe.generic.auto._
  implicit def xmlToOptionT[A](x0: Either[String, NodeSeq])(implicit decoder: Lazy[Decoder[A]]): Option[A] =
    x0 match {
      case Right(xml) =>
        val m = parseEntity(xml)

        implicit val d: Decoder[A] = decoder.value
        CaseClassFromMap.mapToCaseClass[A](m) match {
          case Right(a) => Some(a)
          case _ => None
        }
      case _ => None
    }

  "" should {
    "" in {

      val xml = <mainspan><entity poref="class:1234" type="Person">
          <attr name="firstName"><string value="Mickey" /></attr>
        <attr name="lastName" shared="False" key="True">
          <string value="Mouse"/>
          <renderedValue>Teresa Laury</renderedValue>
        </attr>
        <attr name="modifiers" shared="False" key="True" weaksetelements="Either">
            <persistentReference poref="EntitySet:4122CE2401D74D48B6B48DAAC8514ADF"/>
            <renderedValue>set of Modifier</renderedValue>
          </attr>
          <attr name="owner" shared="False" key="True">
            <entityreference poref="Person:5F2899A431027C4F8426CAB5A6A278B7" type="Person"/>
            <renderedValue>
              ABC123
            </renderedValue>
          </attr>
        <attr name="customAttributes" shared="False" key="True">
          <entityreference poref="Entity:0101" type="Person_CustomAttributesManager"/>
          <renderedValue>
            ABC123
          </renderedValue>
        </attr>
          <attr name="employer" shared="False" key="True">
            <entityreference poref="Party:5F2899A431027C4F8426CAB5A6A278B7" type="Company"/>
            <renderedValue>
              Disney
            </renderedValue>
          </attr>
          <attr name="targetURL" shared="False" key="True">
            <documentProxy poref="DocumentProxy:4668CF5B769C8E44B16B3200B5EDC582"/>
            <renderedValue>/tmp/some.txt
            </renderedValue>
          </attr>
          <attr name="dateOfBirth" shared="False" key="True">
            <date value="1386944623418"/>
            <renderedValue>12/13/2013 09:23</renderedValue>
          </attr>
        </entity></mainspan>

      val m = parseEntity(xml)

      //
      import io.circe.generic.auto._
      val maybePerson = CaseClassFromMap.mapToCaseClass[CkPerson](m)

      val p = maybePerson match {
        case Right(a) => a
        case Left(e) => CkPerson()
      }

      maybePerson should beRight

      val ei: Either[String, NodeSeq] = Right(xml)
      val p2 = xmlToOptionT[CkPerson](ei)

      p2 should beSome
    }
  }


}
