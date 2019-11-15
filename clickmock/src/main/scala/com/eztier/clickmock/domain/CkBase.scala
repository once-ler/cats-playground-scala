package com.eztier.clickmock
package domain

import java.time.{ZoneId, ZonedDateTime}
import java.util.Date
import scala.xml.NodeSeq

// com.webridge.entity.Entity:
case class EntityReference[A <: Any](Poref: String = "", Type: String = "")

// com.webridge.eset.EntitySet:
case class PersistentReference(Poref: String = "")

// Use for from NodeSeq to Ck... types
case class WrappedEntityXml(xml: NodeSeq)
case class WrappedEntitySetXml(xml: NodeSeq)

case class Ck_NYUGenderSelection_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  name: String = "",
  enabled: Int = 1
) extends CkBase with WithEncoder

case class Ck_NYUGenderSelection
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_NYUGenderSelection_CustomAttributesManager] = EntityReference[Ck_NYUGenderSelection_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_NYUParticipantEthnicity_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  name: String = "",
  enabled: Int = 1,
  ctrpCode: String = ""
) extends CkBase with WithEncoder

case class Ck_NYUParticipantEthnicity(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_NYUParticipantEthnicity_CustomAttributesManager] = EntityReference[Ck_NYUParticipantEthnicity_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_NYUParticipantRace_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  name: String = "",
  isActive: Int = 1,
  ctrpCode: String = ""
) extends CkBase with WithEncoder

case class Ck_NYUParticipantRace
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_NYUParticipantRace_CustomAttributesManager] = EntityReference[Ck_NYUParticipantRace_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_PersonCustomExtension_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  gender: EntityReference[Ck_NYUGenderSelection] = EntityReference[Ck_NYUGenderSelection]()
) extends CkBase with WithEncoder

case class Ck_PersonCustomExtension(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_PersonCustomExtension_CustomAttributesManager] = EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: String) = ???
}

case class CkCompany
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  name: String = "",
) extends CkBase with WithEncoder with WithNonProject

case class CkPerson_CustomAttributesManager(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  personCustomExtension: EntityReference[Ck_PersonCustomExtension] = EntityReference[Ck_PersonCustomExtension]()
) extends CkBase with WithEncoder

case class CkPerson(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  employer: EntityReference[CkCompany] = EntityReference[CkCompany](),
  firstName: String = "",
  lastName: String = "",
  middleName: String = "",
  customAttributes: EntityReference[CkPerson_CustomAttributesManager] = EntityReference[CkPerson_CustomAttributesManager](),
  dateOfBirth: Date = new Date(),
  gender: String = "Male"
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: String) = ???
}

case class CkState
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  shortName: String = ""
) extends CkBase with WithEncoder

case class Ck_ClickAddress_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  address1: String = "",
  city: String = "",
  postalCode: String = "",
  state: EntityReference[CkState] = EntityReference[CkState]()
) extends CkBase with WithEncoder

case class Ck_ClickAddress
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_ClickAddress_CustomAttributesManager] = EntityReference[Ck_ClickAddress_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: String) = ???
}

case class Ck_ClickPartyContactInformation_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  homeAddress: EntityReference[Ck_ClickAddress] = EntityReference[Ck_ClickAddress](),
  phoneHome: String = "",
  preferredEmail: String = ""
) extends CkBase with WithEncoder

case class Ck_ClickPartyContactInformation
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager] = EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: String) = ???
}

case class CkCountry
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  isocode: String = "",
  _webrUnique_ID: String = ""
) extends CkBase with WithEncoder

case class CkPostalContactInformation
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  city: String = "",
  postalCode: String = "",
  address1: String = "",
  stateProvince: EntityReference[CkState] = EntityReference[CkState](),
  country: EntityReference[CkCountry] = EntityReference[CkCountry]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Postal Contact Information"
}

case class CkPhoneContactInformation(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  areaCode: String = "",
  phoneNumber: String = "",
  country: EntityReference[CkCountry] = EntityReference[CkCountry]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Phone Contact Information"
}

case class CkEmailContactInformation
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  eMailAddress: String = ""
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "E-mail Contact Information"
}

case class CkPartyContactInformation
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  phoneHome: EntityReference[CkPhoneContactInformation] = EntityReference[CkPhoneContactInformation](),
  emailPreferred: EntityReference[CkEmailContactInformation] = EntityReference[CkEmailContactInformation](),
  addressHome: EntityReference[CkPostalContactInformation] = EntityReference[CkPostalContactInformation]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "PartyContactInformation"
}

case class CkParty
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  contactInformation: EntityReference[CkPartyContactInformation] = EntityReference[CkPartyContactInformation]()
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Party"
}

case class CkClassification
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = ""
) extends CkBase with WithEncoder

case class CkProject
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  status: EntityReference[CkClassification] = EntityReference[CkClassification]()
) extends CkBase with WithEncoder

case class CkResource
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  dateModified: Date = Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant),
  dateCreated: Date =  Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant)
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension_CustomAttributesManager(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  particpantEthnicity: EntityReference[Ck_NYUParticipantEthnicity] = EntityReference[Ck_NYUParticipantEthnicity](),
  participantRace: EntityReference[Ck_NYUParticipantRace] = EntityReference[Ck_NYUParticipantRace]()
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  ID: String = "",
  customAttributes: EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager] = EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById {
  def findById(id: String) = ???
}

case class Ck_Participant_CustomAttributesManager
(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  medicalRecordNumber: String = "",
  person: EntityReference[CkPerson] = EntityReference[CkPerson](),
  participantCustomExtension: EntityReference[Ck_ParticipantCustomExtension] = EntityReference[Ck_ParticipantCustomExtension]()
) extends CkBase with WithEncoder with WithFindByMrn {
  def findByMrn(id: String) = ???
}

case class Ck_Participant(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  _webrUnique_ID: String = "",
  customAttributes: EntityReference[Ck_Participant_CustomAttributesManager] = EntityReference[Ck_Participant_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById {
  def findById(id: String) = ???
}

case class Ck_ParticipantRecord_CustomAttributesManager(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  participant: EntityReference[Ck_Participant] = EntityReference[Ck_Participant](),
  partyContactInformation: EntityReference[Ck_ClickPartyContactInformation] = EntityReference[Ck_ClickPartyContactInformation](),
  clinicalTrial: EntityReference[Ck_ClinicalTrial] = EntityReference[Ck_ClinicalTrial]()
) extends CkBase with WithEncoder

case class Ck_ParticipantRecord(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  _webrUnique_ID: String = "",
  customAttributes: EntityReference[Ck_ParticipantRecord_CustomAttributesManager] = EntityReference[Ck_ParticipantRecord_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject

case class Ck_ClinicalTrial_CustomAttributesManager(
  oid: String = "",
  Class: String = "",
  extent: String = ""
) extends CkBase with WithEncoder

case class Ck_ClinicalTrial(
  oid: String = "",
  Class: String = "",
  extent: String = "",
  _webrUnique_ID: String = "",
  customAttributes: EntityReference[Ck_ClinicalTrial_CustomAttributesManager] = EntityReference[Ck_ClinicalTrial_CustomAttributesManager]()
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById {
  def findById(id: String) = ???
}
