package com.eztier.clickmock
package domain

import cats.Functor
import cats.data.{EitherT, OptionT}
import scala.xml.NodeSeq

import infrastructure.soap.CkXmlToTypeImplicits

// Ck_PersonCustomExtension
trait Ck_PersonCustomExtensionAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)]
  def findByOid(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)]
}

class Ck_PersonCustomExtensionService[F[_]](repo: Ck_PersonCustomExtensionAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_PersonCustomExtension])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_PersonCustomExtension])} not found.")
}

object Ck_PersonCustomExtensionService {
  def apply[F[_]](repo: Ck_PersonCustomExtensionAlgebra[F]): Ck_PersonCustomExtensionService[F] = new Ck_PersonCustomExtensionService(repo)
}

// CkPerson
trait CkPersonAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, (CkPerson, CkPerson_CustomAttributesManager)]
  def findByOid(id: Option[String]): OptionT[F, (CkPerson, CkPerson_CustomAttributesManager)]
}

class CkPersonService[F[_]](repo: CkPersonAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (CkPerson, CkPerson_CustomAttributesManager)] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkPerson])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (CkPerson, CkPerson_CustomAttributesManager)] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkPerson])} not found.")
}

object CkPersonService {
  def apply[F[_]](repo: CkPersonAlgebra[F]): CkPersonService[F] = new CkPersonService(repo)
}

// Ck_ClickAddress
trait Ck_ClickAddressAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, Ck_ClickAddress]
  def findByOid(id: Option[String]): OptionT[F, Ck_ClickAddress]
}

class Ck_ClickAddressService[F[_]](repo: Ck_ClickAddressAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClickAddress] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClickAddress])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClickAddress] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClickAddress])} not found.")
}

object Ck_ClickAddressService {
  def apply[F[_]](repo: Ck_ClickAddressAlgebra[F]): Ck_ClickAddressService[F] = new Ck_ClickAddressService(repo)
}

// Ck_ClickPartyContactInformation
trait Ck_ClickPartyContactInformationAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, Ck_ClickPartyContactInformation]
  def findByOid(id: Option[String]): OptionT[F, Ck_ClickPartyContactInformation]
}

class Ck_ClickPartyContactInformationService[F[_]](repo: Ck_ClickPartyContactInformationAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClickPartyContactInformation] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClickPartyContactInformation])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClickPartyContactInformation] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClickPartyContactInformation])} not found.")
}

object Ck_ClickPartyContactInformationService {
  def apply[F[_]](repo: Ck_ClickPartyContactInformationAlgebra[F]): Ck_ClickPartyContactInformationService[F] = new Ck_ClickPartyContactInformationService(repo)
}

// Ck_ParticipantCustomExtension
trait Ck_ParticipantCustomExtensionAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)]
  def findByOid(id: Option[String]): OptionT[F, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)]
}

class Ck_ParticipantCustomExtensionService[F[_]](repo: Ck_ParticipantCustomExtensionAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ParticipantCustomExtension])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ParticipantCustomExtension])} not found.")
}

object Ck_ParticipantCustomExtensionService {
  def apply[F[_]](repo: Ck_ParticipantCustomExtensionAlgebra[F]): Ck_ParticipantCustomExtensionService[F] = new Ck_ParticipantCustomExtensionService(repo)
}

// Ck_Participant
trait Ck_ParticipantAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)]
  def findByOid(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)]
}

class Ck_ParticipantService[F[_]](repo: Ck_ParticipantAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_Participant])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_Participant])} not found.")
}

object Ck_ParticipantService {
  def apply[F[_]](repo: Ck_ParticipantAlgebra[F]): Ck_ParticipantService[F] = new Ck_ParticipantService(repo)
}

// Ck_ClinicalTrial
trait Ck_ClinicalTrialAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, Ck_ClinicalTrial]
  def findByOid(id: Option[String]): OptionT[F, Ck_ClinicalTrial]
}

class Ck_ClinicalTrialService[F[_]](repo: Ck_ClinicalTrialAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClinicalTrial] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClinicalTrial])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ClinicalTrial] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ClinicalTrial])} not found.")
}

object Ck_ClinicalTrialService {
  def apply[F[_]](repo: Ck_ClinicalTrialAlgebra[F]): Ck_ClinicalTrialService[F] = new Ck_ClinicalTrialService(repo)
}

// Ck_Participant_CustomAttributesManager (findByMrn)
trait Ck_Participant_CustomAttributesManagerAlgebra[F[_]] {
  def findByMrn(id: Option[String]): F[List[Ck_Participant_CustomAttributesManager]]
}

class Ck_Participant_CustomAttributesManagerService[F[_]](repo: Ck_Participant_CustomAttributesManagerAlgebra[F]) {
  def findByMrn(id: Option[String])(implicit F: Functor[F]): F[List[Ck_Participant_CustomAttributesManager]] =
    repo
      .findByMrn(id)
}

object Ck_Participant_CustomAttributesManagerService {
  def apply[F[_]](repo: Ck_Participant_CustomAttributesManagerAlgebra[F]): Ck_Participant_CustomAttributesManagerService[F] = new Ck_Participant_CustomAttributesManagerService(repo)
}

// CkParty
trait CkPartyAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, (CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)]
  def findByOid(id: Option[String]): OptionT[F, (CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)]
}

class CkPartyService[F[_]](repo: CkPartyAlgebra[F]) {
  // There is no ID field.
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)] =
    EitherT.left(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkParty])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, (CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkParty])} not found.")
}

object CkPartyService {
  def apply[F[_]](repo: CkPartyAlgebra[F]): CkPartyService[F] = new CkPartyService(repo)
}

// CkResource
trait CkResourceAlgebra[F[_]] {
  def findById(id: Option[String]): OptionT[F, CkResource]
  def findByOid(id: Option[String]): OptionT[F, CkResource]
}

class CkResourceService[F[_]](repo: CkResourceAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, CkResource] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkResource])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, CkResource] =
    repo
      .findByOid(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[CkResource])} not found.")
}

object CkResourceService {
  def apply[F[_]](repo: CkResourceAlgebra[F]): CkResourceService[F] = new CkResourceService(repo)
}

// Soap service
trait CkEntityAlgebra[F[_]] {
  // Get Entity
  def getEntity(oid: String): F[NodeSeq]

  // Redefine Entity
  def redefineEntity(oid: String, xmlString: String): F[NodeSeq]

  def redefineEntity[A <: CkBase with WithEncoder](in: A): F[NodeSeq]

  def redefineCompleteEntity[A <: CkBase with WithCustomAttributes with WithEncoder, B <: CkBase with WithEncoder](root: A, child: B): F[NodeSeq]

  // Create Entity
  def createEntity(typeName: String, xmlString: String): F[NodeSeq]

  def createEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A): F[NodeSeq]

  def createCompleteEntity[A <: WithEncoder, B <: WithEncoder](inner: A, outer: B): F[NodeSeq]
}

class CkEntityService[F[_]](repo: CkEntityAlgebra[F]) {
  def getEntity(oid: String): F[NodeSeq] = repo.getEntity(oid)
  def redefineEntity(oid: String, xmlString: String): F[NodeSeq] = repo.redefineEntity(oid, xmlString)
  def redefineEntity[A <: CkBase with WithEncoder](in: A): F[NodeSeq] = repo.redefineEntity(in)
  def redefineCompleteEntity[A <: CkBase with WithCustomAttributes with WithEncoder, B <: CkBase with WithEncoder](root: A, child: B): F[NodeSeq] = redefineCompleteEntity(root, child)
  def createEntity(typeName: String, xmlString: String): F[NodeSeq] = repo.createEntity(typeName, xmlString)
  def createEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A): F[NodeSeq] = repo.createEntity(in)
  def createCompleteEntity[A <: WithEncoder, B <: WithEncoder](inner: A, outer: B): F[NodeSeq] = repo.createCompleteEntity(inner, outer)
}

object CkEntityService {
  def apply[F[_]](repo: CkEntityAlgebra[F]): CkEntityService[F] = new CkEntityService(repo)
}
