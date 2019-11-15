package com.eztier.clickmock
package domain

object CkImplicits {

  implicit class CopyToCkPerson(in: CkPerson) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkPerson => in.copy(ID = a.ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case a: CkCompany => in.copy(employer = EntityReference[CkCompany](Poref = a.Class + ":" + a.oid, Type = classOf[CkCompany].getSimpleName))
        case a: CkPerson_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference(Poref = a.Class + ":" + a.oid, Type = classOf[CkPerson_CustomAttributesManager].getSimpleName))
        case _ => in
      }
    }
  }

  implicit class CopyToCkPerson_CustomAttributesManager(in: CkPerson_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_PersonCustomExtension => in.copy(personCustomExtension = EntityReference[Ck_PersonCustomExtension](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_PersonCustomExtension].getSimpleName))
        case a: CkPerson_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkPerson_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_PersonCustomExtension] => merge(a)
        case Some(a) if a.getClass == classOf[CkPerson_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_Participant(in: Ck_Participant) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_Participant => in.copy(_webrUnique_ID = a._webrUnique_ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case a: Ck_Participant_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference[Ck_Participant_CustomAttributesManager](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_Participant_CustomAttributesManager].getSimpleName))
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_Participant = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_Participant] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_Participant_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_Participant_CustomAttributesManager(in: Ck_Participant_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkPerson => in.copy(person = EntityReference[CkPerson](Poref = a.Class + ":" + a.oid, Type = classOf[CkPerson]))
        case a: Ck_ParticipantCustomExtension => in.copy(participantCustomExtension = EntityReference[Ck_ParticipantCustomExtension](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ParticipantCustomExtension].getSimpleName))
        case a: Ck_Participant_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_Participant_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[CkPerson] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ParticipantCustomExtension] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_Participant_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ParticipantRecord_CustomAttributesManager(in: Ck_ParticipantRecord_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_Participant => in.copy(participant = EntityReference[Ck_Participant](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_Participant].getSimpleName))
        case a: Ck_ClickPartyContactInformation => in.copy(partyContactInformation = EntityReference[Ck_ClickPartyContactInformation](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ClickPartyContactInformation].getSimpleName))
        case a: Ck_ParticipantRecord_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ParticipantRecord_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_Participant] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ClickPartyContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ParticipantRecord_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCkPostalContactInformation(in: CkPostalContactInformation) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkState => in.copy(stateProvince = EntityReference[CkState](Poref = a.Class + ":" + a.oid, Type = classOf[CkState].getSimpleName))
        case a: CkPostalContactInformation => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkPostalContactInformation = {
      diff match {
        case Some(a) if a.getClass == classOf[CkState] => merge(a)
        case Some(a) if a.getClass == classOf[CkPostalContactInformation] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCkEmailContactInformation(in: CkEmailContactInformation) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkEmailContactInformation => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkEmailContactInformation = {
      diff match {
        case Some(a) if a.getClass == classOf[CkEmailContactInformation] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCkPhoneContactInformation(in: CkPhoneContactInformation) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkPhoneContactInformation => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkPhoneContactInformation = {
      diff match {
        case Some(a) if a.getClass == classOf[CkPhoneContactInformation] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCkPartyContactInformation(in: CkPartyContactInformation) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkPostalContactInformation => in.copy(addressHome = EntityReference[CkPostalContactInformation](Poref = a.Class + ":" + a.oid, Type = classOf[CkPostalContactInformation].getSimpleName))
        case a: CkEmailContactInformation => in.copy(emailPreferred = EntityReference[CkEmailContactInformation](Poref = a.Class + ":" + a.oid, Type = classOf[CkEmailContactInformation].getSimpleName))
        case a: CkPhoneContactInformation => in.copy(phoneHome = EntityReference[CkPhoneContactInformation](Poref = a.Class + ":" + a.oid, Type = classOf[CkPhoneContactInformation].getSimpleName))
        case a: CkPartyContactInformation => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkPartyContactInformation = {
      diff match {
        case Some(a) if a.getClass == classOf[CkPostalContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[CkEmailContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[CkPhoneContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[CkPartyContactInformation] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCkParty(in: CkParty) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkPartyContactInformation => in.copy(contactInformation = EntityReference[CkPartyContactInformation](Poref = a.Class + ":" + a.oid, Type = classOf[CkPartyContactInformation].getSimpleName))
        case a: CkParty => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): CkParty = {
      diff match {
        case Some(a) if a.getClass == classOf[CkPartyContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[CkParty] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ClickAddress(in: Ck_ClickAddress) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_ClickAddress_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference[Ck_ClickAddress_CustomAttributesManager](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ClickAddress_CustomAttributesManager].getSimpleName))
        case a: Ck_ClickAddress => in.copy(ID = a.ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ClickAddress = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_ClickAddress_CustomAttributesManager] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ClickAddress] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ClickAddress_CustomAttributesManager(in: Ck_ClickAddress_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: CkState => in.copy(state = EntityReference[CkState](Poref = a.Class + ":" + a.oid, Type = classOf[CkState].getSimpleName))
        case a: Ck_ClickAddress_CustomAttributesManager => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ClickAddress_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[CkState] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ClickAddress_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ClickPartyContactInformation(in: Ck_ClickPartyContactInformation) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_ClickPartyContactInformation => in.copy(ID = a.ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case a: Ck_ClickPartyContactInformation_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference[Ck_ClickPartyContactInformation_CustomAttributesManager](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ClickPartyContactInformation_CustomAttributesManager].getSimpleName))
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ClickPartyContactInformation = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_ClickPartyContactInformation] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ClickPartyContactInformation_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ClickPartyContactInformation_CustomAttributesManager(in: Ck_ClickPartyContactInformation_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_ClickAddress => in.copy(homeAddress = EntityReference[Ck_ClickAddress](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ClickAddress].getSimpleName))
        case a: Ck_ClickPartyContactInformation_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ClickPartyContactInformation_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_ClickAddress] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ClickPartyContactInformation_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_PersonCustomExtension(in: Ck_PersonCustomExtension) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_PersonCustomExtension => in.copy(ID = a.ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case a: Ck_PersonCustomExtension_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference[Ck_PersonCustomExtension_CustomAttributesManager](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_PersonCustomExtension_CustomAttributesManager].getSimpleName))
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_PersonCustomExtension = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_PersonCustomExtension] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_PersonCustomExtension_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_PersonCustomExtension_CustomAttributesManager(in: Ck_PersonCustomExtension_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_NYUGenderSelection => in.copy(gender = EntityReference[Ck_NYUGenderSelection](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_NYUGenderSelection].getSimpleName))
        case a: Ck_PersonCustomExtension_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_PersonCustomExtension_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_NYUGenderSelection] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_PersonCustomExtension_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ParticipantCustomExtension(in: Ck_ParticipantCustomExtension) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_ParticipantCustomExtension => in.copy(ID = a.ID, oid = a.oid, Class = a.Class, customAttributes = a.customAttributes)
        case a: Ck_ParticipantCustomExtension_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(customAttributes = EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_ParticipantCustomExtension_CustomAttributesManager].getSimpleName))
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ParticipantCustomExtension = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_ParticipantCustomExtension] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ParticipantCustomExtension_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

  implicit class CopyToCk_ParticipantCustomExtension_CustomAttributesManager(in: Ck_ParticipantCustomExtension_CustomAttributesManager) {
    def merge[T <: CkBase](diff: T) = {
      diff match {
        case a: Ck_NYUParticipantEthnicity => in.copy(particpantEthnicity = EntityReference[Ck_NYUParticipantEthnicity](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_NYUParticipantEthnicity].getSimpleName))
        case a: Ck_NYUParticipantRace => in.copy(participantRace = EntityReference[Ck_NYUParticipantRace](Poref = a.Class + ":" + a.oid, Type = classOf[Ck_NYUParticipantRace].getSimpleName))
        case a: Ck_ParticipantCustomExtension_CustomAttributesManager if a.oid != null && a.oid.length > 0 => in.copy(oid = a.oid, Class = a.Class)
        case _ => in
      }
    }

    def merge[T <: CkBase](diff: Option[T]): Ck_ParticipantCustomExtension_CustomAttributesManager = {
      diff match {
        case Some(a) if a.getClass == classOf[Ck_NYUParticipantEthnicity] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_NYUParticipantRace] => merge(a)
        case Some(a) if a.getClass == classOf[Ck_ParticipantCustomExtension_CustomAttributesManager] => merge(a)
        case _ => in
      }
    }
  }

}
