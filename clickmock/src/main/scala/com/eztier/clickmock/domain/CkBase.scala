package com.eztier.clickmock
package domain

import java.time.{ZoneId, ZonedDateTime}
import java.util.Date
import scala.xml.NodeSeq

// com.webridge.entity.Entity:
case class EntityReference[A <: Any](Poref: Option[String] = None, Type: Option[String] = None)

// com.webridge.eset.EntitySet:
case class PersistentReference(Poref: Option[String] = None)

// Use for from NodeSeq to Ck... types
case class WrappedEntityXml(xml: NodeSeq)
case class WrappedEntitySetXml(xml: NodeSeq)

case class Ck_NYUGenderSelection_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  enabled: Option[Int] = Some(1)
) extends CkBase with WithEncoder

case class Ck_NYUGenderSelection
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_NYUGenderSelection_CustomAttributesManager] = EntityReference[Ck_NYUGenderSelection_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_NYUParticipantEthnicity_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  enabled: Option[Int] = Some(1),
  ctrpCode: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_NYUParticipantEthnicity(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_NYUParticipantEthnicity_CustomAttributesManager] = EntityReference[Ck_NYUParticipantEthnicity_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_NYUParticipantRace_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  isActive: Option[Int] = Some(1),
  ctrpCode: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_NYUParticipantRace
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_NYUParticipantRace_CustomAttributesManager] = EntityReference[Ck_NYUParticipantRace_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_PersonCustomExtension_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  gender: EntityReference[Ck_NYUGenderSelection] = EntityReference[Ck_NYUGenderSelection]()
) extends CkBase with WithEncoder

case class Ck_PersonCustomExtension(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_PersonCustomExtension_CustomAttributesManager] = EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class CkCompany
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  name: Option[String] = None,
) extends CkBase with WithEncoder with WithNonProject

case class CkPerson_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  personCustomExtension: EntityReference[Ck_PersonCustomExtension] = EntityReference[Ck_PersonCustomExtension]()
) extends CkBase with WithEncoder

case class CkPerson(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  employer: EntityReference[CkCompany] = EntityReference[CkCompany](),
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  middleName: Option[String] = None,
  customAttributes: EntityReference[CkPerson_CustomAttributesManager] = EntityReference[CkPerson_CustomAttributesManager](),
  dateOfBirth: Date = new Date(),
  gender: String = "Male"
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class CkState
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  shortName: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ClickAddress_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  address1: Option[String] = None,
  city: Option[String] = None,
  postalCode: Option[String] = None,
  state: EntityReference[CkState] = EntityReference[CkState]()
) extends CkBase with WithEncoder

case class Ck_ClickAddress
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_ClickAddress_CustomAttributesManager] = EntityReference[Ck_ClickAddress_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class Ck_ClickPartyContactInformation_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  homeAddress: EntityReference[Ck_ClickAddress] = EntityReference[Ck_ClickAddress](),
  phoneHome: Option[String] = None,
  preferredEmail: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ClickPartyContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager] = EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class CkCountry
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  isocode: Option[String] = None,
  _webrUnique_ID: Option[String] = None
) extends CkBase with WithEncoder

case class CkPostalContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  city: Option[String] = None,
  postalCode: Option[String] = None,
  address1: Option[String] = None,
  stateProvince: EntityReference[CkState] = EntityReference[CkState](),
  country: EntityReference[CkCountry] = EntityReference[CkCountry]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = Some("Postal Contact Information")
}

case class CkPhoneContactInformation(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  areaCode: Option[String] = None,
  phoneNumber: Option[String] = None,
  country: EntityReference[CkCountry] = EntityReference[CkCountry]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = Some("Phone Contact Information")
}

case class CkEmailContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  eMailAddress: Option[String] = None
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = Some("E-mail Contact Information")
}

case class CkPartyContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  phoneHome: EntityReference[CkPhoneContactInformation] = EntityReference[CkPhoneContactInformation](),
  emailPreferred: EntityReference[CkEmailContactInformation] = EntityReference[CkEmailContactInformation](),
  addressHome: EntityReference[CkPostalContactInformation] = EntityReference[CkPostalContactInformation]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = Some("PartyContactInformation")
}

case class CkParty
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  contactInformation: EntityReference[CkPartyContactInformation] = EntityReference[CkPartyContactInformation]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = Some("Party")
}

case class CkClassification
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None
) extends CkBase with WithEncoder

case class CkProject
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  status: EntityReference[CkClassification] = EntityReference[CkClassification]()
) extends CkBase with WithEncoder

case class CkResource
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  dateModified: Date = Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant),
  dateCreated: Date =  Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant)
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  particpantEthnicity: EntityReference[Ck_NYUParticipantEthnicity] = EntityReference[Ck_NYUParticipantEthnicity](),
  participantRace: EntityReference[Ck_NYUParticipantRace] = EntityReference[Ck_NYUParticipantRace]()
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager] = EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class Ck_Participant_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  medicalRecordNumber: Option[String] = None,
  person: EntityReference[CkPerson] = EntityReference[CkPerson](),
  participantCustomExtension: EntityReference[Ck_ParticipantCustomExtension] = EntityReference[Ck_ParticipantCustomExtension]()
) extends CkBase with WithEncoder with WithFindByMrn {
  def findByMrn(id: Option[String]) = ???
}

case class Ck_Participant(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  _webrUnique_ID: Option[String] = None,
  customAttributes: EntityReference[Ck_Participant_CustomAttributesManager] = EntityReference[Ck_Participant_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById {
  def findById(id: Option[String]) = ???
}

case class Ck_ParticipantRecord_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  participant: EntityReference[Ck_Participant] = EntityReference[Ck_Participant](),
  partyContactInformation: EntityReference[Ck_ClickPartyContactInformation] = EntityReference[Ck_ClickPartyContactInformation](),
  clinicalTrial: EntityReference[Ck_ClinicalTrial] = EntityReference[Ck_ClinicalTrial]()
) extends CkBase with WithEncoder

case class Ck_ParticipantRecord(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  _webrUnique_ID: Option[String] = None,
  customAttributes: EntityReference[Ck_ParticipantRecord_CustomAttributesManager] = EntityReference[Ck_ParticipantRecord_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject

case class Ck_ClinicalTrial_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ClinicalTrial(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  _webrUnique_ID: Option[String] = None,
  customAttributes: EntityReference[Ck_ClinicalTrial_CustomAttributesManager] = EntityReference[Ck_ClinicalTrial_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById {
  def findById(id: Option[String]) = ???
}
