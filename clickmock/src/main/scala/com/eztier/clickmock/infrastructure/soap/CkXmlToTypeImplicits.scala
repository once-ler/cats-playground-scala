package com.eztier.clickmock
package infrastructure.soap

import cats.syntax.option._
import scala.reflect.runtime.universe._
import java.util.Date
import scala.xml.NodeSeq

import domain._

object CkXmlToTypeImplicits {
  implicit def toCkTypeName(in: Class[_]) = in.getSimpleName.replace("Ck", "")

  implicit class WrapCkType[T <: CkBase](in: Class[T])(implicit typeTag: TypeTag[T]) {
    def getCkClassName = typeTag.tpe match {
      case a if a == typeOf[CkPerson] => "com.webridge.account.Person:"
      case a if a == typeOf[CkCompany] => "com.webridge.account.Party:"
      case _ => "com.webridge.entity.Entity:"
    }
  }

  private def parseEntity(root: NodeSeq): Map[String, Any] = {
    val entity = root \ "entity"
    val r = entity \ "attr"
    val m = (Map[String, Any]() /: r) {
      (a, n) =>
        val h = n.child.head

        val e = h.label match {
          case "entityreference" | "persistentReference" => Map("poref"-> h.attribute("poref").getOrElse("").toString, "type"-> h.attribute("type").getOrElse("").toString)
          case "date" => new Date(h.attribute("value").getOrElse("0").toString.toLong)
          case _ => h.attribute("value").getOrElse("").toString
        }

        a + (n.attribute("name").getOrElse("").toString -> e)
    }

    val poref = (entity \ "@poref").headOption.getOrElse("").toString
    val pattern = "(.+):(.+)".r
    val pattern(clz, oid) = poref

    m ++ Map(
      "oid" -> oid,
      "Class" -> clz,
      "type" -> (entity \ "@type").headOption.getOrElse("").toString
    )
  }

  private def maybeEntity(x: Map[String, Any], attrName: String) =
    x.get(attrName) match {
      case Some(a) => x(attrName).asInstanceOf[Map[String, String]]
      case None => Map("poref" -> "", "type" -> "")
    }

  implicit def fromEntityXmlToCkPhoneContactInformation(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    CkPhoneContactInformation(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      phoneNumber = x("phoneNumber").asInstanceOf[String].some
    )
  }

  implicit def fromEntityXmlToCkEmailContactInformation(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    CkEmailContactInformation(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      eMailAddress = x("eMailAddress").asInstanceOf[String].some
    )
  }

  implicit def fromEntityXmlToCkPostalContactInformation(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val stateProvince = maybeEntity(x, "stateProvince")
    val country = maybeEntity(x, "country")

    CkPostalContactInformation(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      address1 = x("address1").asInstanceOf[String].some,
      city = x("city").asInstanceOf[String].some,
      postalCode = x("postalCode").asInstanceOf[String].some,
      stateProvince = EntityReference[CkState](Poref = Some(stateProvince("poref")), Type = Some(stateProvince("type"))).some,
      country = EntityReference[CkCountry](Poref = Some(country("poref")), Type = Some(country("type"))).some
    )
  }

  implicit def fromEntityXmlToCkParty(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val contactInformation = maybeEntity(x, "contactInformation")

    CkParty(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      contactInformation = EntityReference[CkPartyContactInformation](Poref = Some(contactInformation("poref")), Type = Some(contactInformation("type"))).some
    )
  }

  implicit def fromEntityXmlToCkPartyContactInformation(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val addressHome = maybeEntity(x, "addressHome")
    val emailPreferred = maybeEntity(x, "emailPreferred")
    val phoneHome = maybeEntity(x, "phoneHome")

    CkPartyContactInformation(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      addressHome = EntityReference[CkPostalContactInformation](Poref = Some(addressHome("poref")), Type = Some(addressHome("type"))).some,
      emailPreferred = EntityReference[CkEmailContactInformation](Poref = Some(emailPreferred("poref")), Type = Some(emailPreferred("type"))).some,
      phoneHome = EntityReference[CkPhoneContactInformation](Poref = Some(phoneHome("poref")), Type = Some(phoneHome("type"))).some
    )
  }

  implicit def fromEntityXmlToCkPerson(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val employer = maybeEntity(x, "employer")
    val customAttributes = maybeEntity(x, "customAttributes")

    CkPerson(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      ID = x("ID").asInstanceOf[String].some,
      dateOfBirth = x("dateOfBirth").asInstanceOf[Date].some,
      firstName = x("firstName").asInstanceOf[String].some,
      lastName = x("lastName").asInstanceOf[String].some,
      middleName = x("middleName").asInstanceOf[String].some,
      gender = x("gender").asInstanceOf[String].some,
      employer = EntityReference[CkCompany](Poref = Some(employer("poref")), Type = Some(employer("type"))).some,
      customAttributes = EntityReference[CkPerson_CustomAttributesManager](Poref = Some(customAttributes("poref")), Type = Some(customAttributes("type"))).some
    )
  }

  implicit def fromEntityXmlToCkPerson_CustomAttributesManager(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val personCustomExtension = maybeEntity(x, "personCustomExtension")

    CkPerson_CustomAttributesManager(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      personCustomExtension = EntityReference[Ck_PersonCustomExtension](Poref = Some(personCustomExtension("poref")), Type = Some(personCustomExtension("type"))).some
    )
  }

  implicit def fromEntityXmlToCk_PersonCustomExtension(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val customAttributes = maybeEntity(x, "customAttributes")

    Ck_PersonCustomExtension(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      ID = x("ID").asInstanceOf[String].some,
      customAttributes = EntityReference[Ck_PersonCustomExtension_CustomAttributesManager](Poref = Some(customAttributes("poref")), Type = Some(customAttributes("type"))).some
    )
  }

  implicit def fromEntityXmlToCk_PersonCustomExtension_CustomAttributesManager(in: WrappedEntityXml) = {
    val x = parseEntity(in.xml)
    val gender = maybeEntity(x, "gender")

    Ck_PersonCustomExtension_CustomAttributesManager(
      oid = x("oid").asInstanceOf[String].some,
      Class = x("Class").asInstanceOf[String].some,
      gender = EntityReference[Ck_GenderSelection](Poref = Some(gender("poref")), Type = Some(gender("type"))).some
    )
  }

}
