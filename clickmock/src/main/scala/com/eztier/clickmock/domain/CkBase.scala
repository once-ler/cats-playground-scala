package com.eztier.clickmock
package domain

import java.time.{ZoneId, ZonedDateTime}
import java.util.Date

import cats.Functor
import cats.data.{EitherT, OptionT}

import scala.xml.NodeSeq
import cats.syntax.option._
import com.eztier.clickmock.infrastructure.soap.CkXmlToTypeImplicits

// com.webridge.entity.Entity:
case class EntityReference[A <: Any](Poref: Option[String] = None, Type: Option[String] = None)

// com.webridge.eset.EntitySet:
case class PersistentReference(Poref: Option[String] = None)

// Use for from NodeSeq to Ck... types
case class WrappedEntityXml(xml: NodeSeq)
case class WrappedEntitySetXml(xml: NodeSeq)

case class Ck_GenderSelection_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  enabled: Option[Int] = 1.some
) extends CkBase with WithEncoder

case class Ck_GenderSelection
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_GenderSelection_CustomAttributesManager]] = EntityReference[Ck_GenderSelection_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_ParticipantEthnicity_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  enabled: Option[Int] = 1.some,
  ctrpCode: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ParticipantEthnicity(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ParticipantEthnicity_CustomAttributesManager]] = EntityReference[Ck_ParticipantEthnicity_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_ParticipantRace_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  name: Option[String] = None,
  isActive: Option[Int] = 1.some,
  ctrpCode: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ParticipantRace
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ParticipantRace_CustomAttributesManager]] = EntityReference[Ck_ParticipantRace_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject

case class Ck_PersonCustomExtension_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  gender: Option[EntityReference[Ck_GenderSelection]] = EntityReference[Ck_GenderSelection]().some
) extends CkBase with WithEncoder

case class Ck_PersonCustomExtension(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]] = EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById

/*
object Ck_PersonCustomExtension extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

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
  personCustomExtension: Option[EntityReference[Ck_PersonCustomExtension]] = EntityReference[Ck_PersonCustomExtension]().some
) extends CkBase with WithEncoder

case class CkPerson(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  employer: Option[EntityReference[CkCompany]] = Some(EntityReference[CkCompany]()),
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  middleName: Option[String] = None,
  customAttributes: Option[EntityReference[CkPerson_CustomAttributesManager]] = EntityReference[CkPerson_CustomAttributesManager]().some,
  dateOfBirth: Option[Date] = Some(new Date()),
  gender: Option[String] = "Male".some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById

/*
object CkPerson extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

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
  state: Option[EntityReference[CkState]] = EntityReference[CkState]().some
) extends CkBase with WithEncoder

case class Ck_ClickAddress
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ClickAddress_CustomAttributesManager]] = EntityReference[Ck_ClickAddress_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById

/*
object Ck_ClickAddress extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

case class Ck_ClickPartyContactInformation_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  homeAddress: Option[EntityReference[Ck_ClickAddress]] = EntityReference[Ck_ClickAddress]().some,
  phoneHome: Option[String] = None,
  preferredEmail: Option[String] = None
) extends CkBase with WithEncoder

case class Ck_ClickPartyContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager]] = EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById

/*
object Ck_ClickPartyContactInformation extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

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
  stateProvince: Option[EntityReference[CkState]] = EntityReference[CkState]().some,
  country: Option[EntityReference[CkCountry]] = EntityReference[CkCountry]().some
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Postal Contact Information".some
}

case class CkPhoneContactInformation(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  areaCode: Option[String] = None,
  phoneNumber: Option[String] = None,
  country: Option[EntityReference[CkCountry]] = EntityReference[CkCountry]().some
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Phone Contact Information".some
}

case class CkEmailContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  eMailAddress: Option[String] = None
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "E-mail Contact Information".some
}

case class CkPartyContactInformation
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  phoneHome: Option[EntityReference[CkPhoneContactInformation]] = EntityReference[CkPhoneContactInformation]().some,
  emailPreferred: Option[EntityReference[CkEmailContactInformation]] = EntityReference[CkEmailContactInformation]().some,
  addressHome: Option[EntityReference[CkPostalContactInformation]] = EntityReference[CkPostalContactInformation]().some
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "PartyContactInformation".some
}

case class CkParty
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  contactInformation: Option[EntityReference[CkPartyContactInformation]] = EntityReference[CkPartyContactInformation]().some
) extends CkBase with WithEncoder with WithExplicitTypeName {
  def typeName = "Party".some
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
  status: Option[EntityReference[CkClassification]] = EntityReference[CkClassification]().some
) extends CkBase with WithEncoder

case class CkResource
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  dateModified: Option[Date] = Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant).some,
  dateCreated: Option[Date] =  Date.from(ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant).some
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  particpantEthnicity: Option[EntityReference[Ck_ParticipantEthnicity]] = EntityReference[Ck_ParticipantEthnicity]().some,
  participantRace: Option[EntityReference[Ck_ParticipantRace]] = EntityReference[Ck_ParticipantRace]().some
) extends CkBase with WithEncoder

case class Ck_ParticipantCustomExtension
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager]] = EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithNonProject with WithFindById

/*
object Ck_ParticipantCustomExtension extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

case class Ck_Participant_CustomAttributesManager
(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  medicalRecordNumber: Option[String] = None,
  person: Option[EntityReference[CkPerson]] = EntityReference[CkPerson]().some,
  participantCustomExtension: Option[EntityReference[Ck_ParticipantCustomExtension]] = EntityReference[Ck_ParticipantCustomExtension]().some
) extends CkBase with WithEncoder

case class Ck_Participant(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  _webrUnique_ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_Participant_CustomAttributesManager]] = EntityReference[Ck_Participant_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById

/*
object Ck_Participant extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

case class Ck_ParticipantRecord_CustomAttributesManager(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  participant: Option[EntityReference[Ck_Participant]] = EntityReference[Ck_Participant]().some,
  partyContactInformation: Option[EntityReference[Ck_ClickPartyContactInformation]] = EntityReference[Ck_ClickPartyContactInformation]().some,
  clinicalTrial: Option[EntityReference[Ck_ClinicalTrial]] = EntityReference[Ck_ClinicalTrial]().some
) extends CkBase with WithEncoder

object Ck_ParticipantRecord_CustomAttributesManager extends WithFindByMrn {
  override def findByMrn(id: Option[String]): List[_] = ???
}

case class Ck_ParticipantRecord(
  oid: Option[String] = None,
  Class: Option[String] = None,
  extent: Option[String] = None,
  _webrUnique_ID: Option[String] = None,
  customAttributes: Option[EntityReference[Ck_ParticipantRecord_CustomAttributesManager]] = EntityReference[Ck_ParticipantRecord_CustomAttributesManager]().some
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
  customAttributes: Option[EntityReference[Ck_ClinicalTrial_CustomAttributesManager]] = EntityReference[Ck_ClinicalTrial_CustomAttributesManager]().some
) extends CkBase with WithEncoder with WithCustomAttributes with WithProject with WithFindById

/*
object Ck_ClinicalTrial extends WithFindById {
  override def findById(id: Option[String]): Option[_] = ???
}
*/

case class CkParticipantRecordAggregate
(
  participantRecord: Option[Ck_ParticipantRecord] = None,
  participantRecordCm: Option[Ck_ParticipantRecord_CustomAttributesManager] = None,
  clickPartyContactInformation: Option[Ck_ClickPartyContactInformation] = None,
  clickPartyContactInformationCm: Option[Ck_ClickPartyContactInformation_CustomAttributesManager] = None,
  clickAddress: Option[Ck_ClickAddress] = None,
  clickAddressCm: Option[Ck_ClickAddress_CustomAttributesManager] = None,
  clinicalTrial: Option[Ck_ClinicalTrial] = None
)

case class CkParticipantAggregate
(
  participant: Option[Ck_Participant] = None,
  participantCm: Option[Ck_Participant_CustomAttributesManager] = None,
  person: Option[CkPerson] = None,
  personCm: Option[CkPerson_CustomAttributesManager] = None,
  personExtension: Option[Ck_PersonCustomExtension] = None,
  personExtensionCm: Option[Ck_PersonCustomExtension_CustomAttributesManager] = None,
  participantExtension: Option[Ck_ParticipantCustomExtension] = None,
  participantExtensionCm: Option[Ck_ParticipantCustomExtension_CustomAttributesManager] = None,
  party: Option[CkParty] = None,
  partyContactInformation: Option[CkPartyContactInformation] = None,
  postalContactInformation: Option[CkPostalContactInformation] = None,
  emailContactInformation: Option[CkEmailContactInformation] = None,
  phoneContactInformation: Option[CkPhoneContactInformation] = None,
  resource: Option[CkResource] = None
)

/*
case class CaToCkTask
(
  mrn: String,
  // serialId: Long = -1,
  ckParticipantResultSet: CkParticipantResultSet = CkParticipantResultSet(),
  ckParticipantRecordResultSet: List[CkParticipantRecordResultSet] = List()
)
*/