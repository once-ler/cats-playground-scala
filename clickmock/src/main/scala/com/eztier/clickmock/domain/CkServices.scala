package com.eztier.clickmock
package domain

import cats.Functor
import cats.data.{EitherT, OptionT}
import com.eztier.clickmock.infrastructure.soap.CkXmlToTypeImplicits

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

// Ck_Person
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
  def findById(id: Option[String]): OptionT[F, Ck_ParticipantCustomExtension]
  def findByOid(id: Option[String]): OptionT[F, Ck_ParticipantCustomExtension]
}

class Ck_ParticipantCustomExtensionService[F[_]](repo: Ck_ParticipantCustomExtensionAlgebra[F]) {
  def findById(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ParticipantCustomExtension] =
    repo
      .findById(id)
      .toRight(s"${CkXmlToTypeImplicits.toCkTypeName(classOf[Ck_ParticipantCustomExtension])} not found.")

  def findByOid(id: Option[String])(implicit F: Functor[F]): EitherT[F, String, Ck_ParticipantCustomExtension] =
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
