package com.eztier.clickmock
package infrastructure.doobie

import doobie._
import doobie.implicits._
import cats.data.{NonEmptyList, OptionT}
import cats.effect.{Bracket, IO}
import cats.syntax.option._
import shapeless._

import domain._

private object Ck_ParticipantSQL {
  implicit val Ck_ParticipantEntityReferenceMeta: Meta[EntityReference[Ck_Participant]] =
    Meta[String].timap(s =>
      EntityReference[Ck_Participant](
        Poref = s.some,
        Type = classOf[Ck_Participant].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_Participant_CustomAttributesManagerEntityReferenceMeta: Meta[EntityReference[Ck_Participant_CustomAttributesManager]] =
    Meta[String].timap(s =>
      EntityReference[Ck_Participant_CustomAttributesManager](
        Poref = s.some,
        Type = classOf[Ck_Participant_CustomAttributesManager].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_ParticipantCustomExtensionEntityReferenceMeta: Meta[EntityReference[Ck_ParticipantCustomExtension]] =
    Meta[String].timap(s =>
      EntityReference[Ck_ParticipantCustomExtension](
        Poref = s.some,
        Type = classOf[Ck_ParticipantCustomExtension].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_ParticipantCustomExtension_CustomAttributesManagerEntityReferenceMeta: Meta[EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager]] =
    Meta[String].timap(s =>
      EntityReference[Ck_ParticipantCustomExtension_CustomAttributesManager](
        Poref = s.some,
        Type = classOf[Ck_ParticipantCustomExtension_CustomAttributesManager].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkCompanyEntityReferenceMeta: Meta[EntityReference[CkCompany]] =
    Meta[String].timap(s =>
      EntityReference[CkCompany](
        Poref = s.some,
        Type = classOf[CkCompany].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkPersonEntityReferenceMeta: Meta[EntityReference[CkPerson]] =
    Meta[String].timap(s =>
      EntityReference[CkPerson](
        Poref = s.some,
        Type = classOf[CkPerson].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkPerson_CustomAttributesManagerEntityReferenceMeta: Meta[EntityReference[CkPerson_CustomAttributesManager]] =
    Meta[String].timap(s =>
      EntityReference[CkPerson_CustomAttributesManager](
        Poref = s.some,
        Type = classOf[CkPerson_CustomAttributesManager].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_PersonCustomExtensionEntityReferenceMeta: Meta[EntityReference[Ck_PersonCustomExtension]] =
    Meta[String].timap(s =>
      EntityReference[Ck_PersonCustomExtension](
        Poref = s.some,
        Type = classOf[Ck_PersonCustomExtension].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_PersonCustomExtension_CustomAttributesManagerEntityReferenceMeta: Meta[EntityReference[Ck_PersonCustomExtension_CustomAttributesManager]] =
    Meta[String].timap(s =>
      EntityReference[Ck_PersonCustomExtension_CustomAttributesManager](
        Poref = s.some,
        Type = classOf[Ck_PersonCustomExtension_CustomAttributesManager].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_NYUGenderSelectionEntityReferenceMeta: Meta[EntityReference[Ck_NYUGenderSelection]] =
    Meta[String].timap(s =>
      EntityReference[Ck_NYUGenderSelection](
        Poref = s.some,
        Type = classOf[Ck_NYUGenderSelection].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  val participantSqlFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a._webrUnique_ID, convert(varchar(50), a.customAttributes, 2) customAttributes,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, b.medicalRecordNumber, convert(varchar(50), b.person, 2) person, convert(varchar(50), b.participantCustomExtension, 2) participantCustomExtension
    from __participant a join __participant_customattributesmanager b on b.oid = a.customattributes where """

  def findByIdSql[Ck_Participant](a: Option[String]): Query0[(Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    (participantSqlFragment ++ fr"_webrunique_id = ${a.getOrElse("")}").query

  def findByOidSql[Ck_Participant](a: Option[String]): Query0[(Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    (participantSqlFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query

  val personSqlFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, convert(varchar(50), a.employer, 2) employer, a.firstName, a.lastName, a.middleName, convert(varchar(50), a.customAttributes, 2) customAttributes, a.dateOfBirth, a.gender,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.personCustomExtension, 2) personCustomExtension
    from _person a join _person_customattributesmanager b on b.oid = a.customattributes where """

  def findByIdSql[CkPerson](a: Option[String]): Query0[(CkPerson, CkPerson_CustomAttributesManager)] =
    (personSqlFragment ++ fr"id = ${a.getOrElse("")}").query

  def findByOidSql[CkPerson](a: Option[String]): Query0[(CkPerson, CkPerson_CustomAttributesManager)] =
    (personSqlFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query

  val personCustomExtensionFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, convert(varchar(50), a.customAttributes, 2) customAttributes,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.gender, 2) gender
    from __personcustomextension a join __personcustomextension_customattributesmanager b on b.oid = a.customattributes where """

  def findByIdSql[Ck_PersonCustomExtension](a: Option[String], isOid: Boolean = false): Query0[(Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    (personCustomExtensionFragment ++ fr"id = ${a.getOrElse("")}").query

  def findByOidSql[Ck_PersonCustomExtension](a: Option[String], isOid: Boolean = false): Query0[(Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    (personCustomExtensionFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query

}

// Ck_Participant
class DoobieCk_ParticipantRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_ParticipantAlgebra[F] {
  import Ck_ParticipantSQL._

  override def findById(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    OptionT(findByIdSql(id).option.transact(xa))

  override def findByOid(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCk_ParticipantRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_ParticipantRepositoryInterpreter[F] =
    new DoobieCk_ParticipantRepositoryInterpreter(xa)
}

// CkPerson
class DoobieCkPersonRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends CkPersonAlgebra[F] {
  import Ck_ParticipantSQL._

  override def findById(id: Option[String]): OptionT[F, (CkPerson, CkPerson_CustomAttributesManager)] =
    OptionT(findByIdSql(id).option.transact(xa))

  override def findByOid(id: Option[String]): OptionT[F, (CkPerson, CkPerson_CustomAttributesManager)] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCkPersonRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCkPersonRepositoryInterpreter[F] =
    new DoobieCkPersonRepositoryInterpreter(xa)
}

// Ck_PersonCustomExtension
class DoobieCk_PersonCustomExtensionRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_PersonCustomExtensionAlgebra[F] {
  import Ck_ParticipantSQL._

  override def findById(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    OptionT(findByIdSql(id).option.transact(xa))

  override def findByOid(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCk_PersonCustomExtensionRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_PersonCustomExtensionRepositoryInterpreter[F] =
    new DoobieCk_PersonCustomExtensionRepositoryInterpreter(xa)
}
