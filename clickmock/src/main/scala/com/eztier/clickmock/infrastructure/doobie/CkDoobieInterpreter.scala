package com.eztier.clickmock
package infrastructure.doobie

// import cats._
// import cats.data._
// import cats.effect._
import cats.implicits._

import doobie._
import doobie.implicits._
import cats.data.{NonEmptyList, OptionT}
import cats.effect.{Bracket, IO}
import shapeless._
import domain._

private object CkDoobieSqlImplicits {
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

  implicit val Ck_GenderSelectionEntityReferenceMeta: Meta[EntityReference[Ck_GenderSelection]] =
    Meta[String].timap(s =>
      EntityReference[Ck_GenderSelection](
        Poref = s.some,
        Type = classOf[Ck_GenderSelection].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkPartyEntityReferenceMeta: Meta[EntityReference[CkParty]] =
    Meta[String].timap(s =>
      EntityReference[CkParty](
        Poref = s.some,
        Type = classOf[CkParty].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val Ck_ClickPartyContactInformationEntityReferenceMeta: Meta[EntityReference[Ck_ClickPartyContactInformation]] =
    Meta[String].timap(s =>
      EntityReference[Ck_ClickPartyContactInformation](
        Poref = s.some,
        Type = classOf[Ck_ClickPartyContactInformation].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkPhoneContactInformationEntityReferenceMeta: Meta[EntityReference[CkPhoneContactInformation]] =
    Meta[String].timap(s =>
      EntityReference[CkPhoneContactInformation](
        Poref = s.some,
        Type = classOf[CkPhoneContactInformation].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkEmailContactInformationEntityReferenceMeta: Meta[EntityReference[CkEmailContactInformation]] =
    Meta[String].timap(s =>
      EntityReference[CkEmailContactInformation](
        Poref = s.some,
        Type = classOf[CkEmailContactInformation].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))

  implicit val CkPostalContactInformationEntityReferenceMeta: Meta[EntityReference[CkPostalContactInformation]] =
    Meta[String].timap(s =>
      EntityReference[CkPostalContactInformation](
        Poref = s.some,
        Type = classOf[CkPostalContactInformation].getSimpleName.replace("Ck", "").some
      )
    )(_.Poref.getOrElse(""))
}

private object Ck_ParticipantSql {
  import CkDoobieSqlImplicits._

  val participantSqlFragment= fr"""select
    a.oid, a.class, a.extent, a._webrUnique_ID, a.customAttributes
    from __participant a where """

  def findByIdSql(a: Option[String]): Query0[Ck_Participant] =
    (participantSqlFragment ++ fr"_webrunique_id = ${a.getOrElse("")}").query

  def findByOidSql(a: Option[String]): Query0[Ck_Participant] =
    (participantSqlFragment ++ fr"a.oid =" ++ Fragment.const(s"X'${a.getOrElse("0")}'")).query
}

private object Ck_ParticipantCmSql {
  import CkDoobieSqlImplicits._

  val participantCmSqlFragment = fr"""select
    b.oid oid2, b.class class2, b.extent extent2, b.medicalRecordNumber, b.person person, b.participantCustomExtension participantCustomExtension
    from __participant_customattributesmanager b where """

  // H2 will try to convert java string to byte string (BLOB).
  def findByOidSql(a: Option[String]): Query0[Ck_Participant_CustomAttributesManager] =
    (participantCmSqlFragment ++ fr"b.oid =" ++ Fragment.const(s"X'${a.getOrElse("0")}'")).query

}

// Ck_Participant
class DoobieCk_ParticipantRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_ParticipantAlgebra[F] {

  override def findById(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)] = {
    val d = for {
      b <- Ck_ParticipantSql.findByIdSql(id).option
      c <- Ck_ParticipantCmSql.findByOidSql(b.getOrElse(Ck_Participant()).customAttributes.get.Poref).option
    } yield b.flatMap(d => c.map(e => (d, e)))

    OptionT(d.transact(xa))
  }

  override def findByOid(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)] = {
    val d = for {
      b <- Ck_ParticipantSql.findByOidSql(id).option
      c <- Ck_ParticipantCmSql.findByOidSql(b.getOrElse(Ck_Participant()).customAttributes.get.Poref).option
    } yield b.flatMap(d => c.map(e => (d, e)))

    OptionT(d.transact(xa))
  }
}

object DoobieCk_ParticipantRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_ParticipantRepositoryInterpreter[F] =
    new DoobieCk_ParticipantRepositoryInterpreter(xa)
}

// Ck_ParticipantCustomExtension
private object Ck_ParticipantCustomExtensionSql {
  import CkDoobieSqlImplicits._

  val participantCustomExtensionSqlFragment = fr"""select
    a.oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, a.customAttributes
    from __participantcustomextension where """

  def findByIdSql(a: Option[String]): Query0[Ck_ParticipantCustomExtension] =
    (participantCustomExtensionSqlFragment ++ fr"_webrunique_id = ${a.getOrElse("")}").query

  def findByOidSql(a: Option[String]): Query0[Ck_ParticipantCustomExtension] =
    (participantCustomExtensionSqlFragment ++ fr"a.oid =" ++ Fragment.const(s"X'${a.getOrElse("0")}'")).query
}

private object Ck_ParticipantCustomExtensionCmSql {
  import CkDoobieSqlImplicits._

  val participantCustomExtensionCmSqlFragment = fr"""select
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.particpantEthnicity, 2) particpantEthnicity, convert(varchar(50), b.participantRace, 2) participantRace
    from __participantcustomextension_customattributesmanager b where """

  def findByOidSql(a: Option[String]): Query0[Ck_ParticipantCustomExtension_CustomAttributesManager] =
    (participantCustomExtensionCmSqlFragment ++ fr"b.oid =" ++ Fragment.const(s"X'${a.getOrElse("0")}'")).query
}

class DoobieCk_ParticipantCustomExtensionRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_ParticipantCustomExtensionAlgebra[F] {

  override def findById(id: Option[String]): OptionT[F, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)] = {
    val d = for {
      b <- Ck_ParticipantCustomExtensionSql.findByIdSql(id).option
      c <- Ck_ParticipantCustomExtensionCmSql.findByOidSql(b.getOrElse(Ck_ParticipantCustomExtension()).customAttributes.get.Poref).option
    } yield b.flatMap(d => c.map(e => (d, e)))

    OptionT(d.transact(xa))
  }

  override def findByOid(id: Option[String]): OptionT[F, (Ck_ParticipantCustomExtension, Ck_ParticipantCustomExtension_CustomAttributesManager)] = {
    val d = for {
      b <- Ck_ParticipantCustomExtensionSql.findByOidSql(id).option
      c <- Ck_ParticipantCustomExtensionCmSql.findByOidSql(b.getOrElse(Ck_ParticipantCustomExtension()).customAttributes.get.Poref).option
    } yield b.flatMap(d => c.map(e => (d, e)))

    OptionT(d.transact(xa))
  }
}

object DoobieCk_ParticipantCustomExtensionRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_ParticipantCustomExtensionRepositoryInterpreter[F] =
    new DoobieCk_ParticipantCustomExtensionRepositoryInterpreter(xa)
}

// CkPerson
private object CkPersonSql {
  val personSqlFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, convert(varchar(50), a.employer, 2) employer, a.firstName, a.lastName, a.middleName, convert(varchar(50), a.customAttributes, 2) customAttributes, a.dateOfBirth, a.gender,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.personCustomExtension, 2) personCustomExtension
    from _person a join _person_customattributesmanager b on b.oid = a.customattributes where """

  def findByIdSql(a: Option[String]): Query0[(CkPerson, CkPerson_CustomAttributesManager)] =
    (personSqlFragment ++ fr"id = ${a.getOrElse("")}").query

  def findByOidSql(a: Option[String]): Query0[(CkPerson, CkPerson_CustomAttributesManager)] =
    (personSqlFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query
}

class DoobieCkPersonRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends CkPersonAlgebra[F] {
  import CkDoobieSqlImplicits._
  import CkPersonSql._

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
private object Ck_PersonCustomExtensionSql {
  val personCustomExtensionFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, convert(varchar(50), a.customAttributes, 2) customAttributes,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.gender, 2) gender
    from __personcustomextension a join __personcustomextension_customattributesmanager b on b.oid = a.customattributes where """

  def findByIdSql(a: Option[String], isOid: Boolean = false): Query0[(Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    (personCustomExtensionFragment ++ fr"id = ${a.getOrElse("")}").query

  def findByOidSql(a: Option[String], isOid: Boolean = false): Query0[(Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    (personCustomExtensionFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query
}

class DoobieCk_PersonCustomExtensionRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_PersonCustomExtensionAlgebra[F] {
  import CkDoobieSqlImplicits._
  import Ck_PersonCustomExtensionSql._

  override def findById(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    OptionT(findByIdSql(id).option.transact(xa))

  override def findByOid(id: Option[String]): OptionT[F, (Ck_PersonCustomExtension, Ck_PersonCustomExtension_CustomAttributesManager)] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCk_PersonCustomExtensionRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_PersonCustomExtensionRepositoryInterpreter[F] =
    new DoobieCk_PersonCustomExtensionRepositoryInterpreter(xa)
}

// CkParty
private object CkPartySql {
  val partySqlFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, convert(varchar(50), a.contactInformation, 2) contactInformation,
    convert(varchar(50), b.oid, 2) oid2, b.class class2, convert(varchar(50), b.extent, 2) extent2, convert(varchar(50), b.phoneHome, 2) phoneHome, convert(varchar(50), b.emailPreferred, 2) emailPreferred, convert(varchar(50), b.addressHome, 2) addressHome,
    convert(varchar(50), c.oid, 2) oid3, c.class class3, convert(varchar(50), c.extent, 2) extent3, c.areaCode, c.phoneNumber, c.country,
    convert(varchar(50), d.oid, 2) oid4, d.class class4, convert(varchar(50), d.extent, 2) extent4, d.emailaddress,
    convert(varchar(50), e.oid, 2) oid5, e.class class5, convert(varchar(50), e.extent, 2) extent5, e.city, e.postalcode, e.address1, e.stateprovince, e.country
    from _party a left join _partycontactinformation b on a.contactInformation = b.oid left join [_phone contact information] c on c.oid = b.phoneHome left join [_e-mail contact information] d on d.oid = b.emailPreferred left join [_postal contact information] e on e.oid = b.addressHome where """

  def findByOidSql(a: Option[String]): Query0[(CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)] =
    (partySqlFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query
}

class DoobieCkPartyRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends CkPartyAlgebra[F] {
  import CkDoobieSqlImplicits._
  import CkPartySql._

  override def findByOid(id: Option[String]): OptionT[F, (CkParty, CkPartyContactInformation, CkPhoneContactInformation, CkEmailContactInformation, CkPostalContactInformation)] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCkPartyRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCkPartyRepositoryInterpreter[F] =
    new DoobieCkPartyRepositoryInterpreter(xa)
}

// CkResource
private object CkResourceSql {
  import CkDoobieSqlImplicits._

  val resourceFragment = fr"""select
    convert(varchar(50), a.oid, 2) oid, a.class, convert(varchar(50), a.extent, 2) extent, a.ID, dateadd(ss, .001*a.dateModified, '1970-01-01') dateModified, , dateadd(ss, .001*a.dateCreated, '1970-01-01') dateCreated where """

  def findByIdSql(a: Option[String], isOid: Boolean = false): Query0[CkResource] =
    (resourceFragment ++ fr"id = ${a.getOrElse("")}").query

  def findByOidSql(a: Option[String], isOid: Boolean = false): Query0[CkResource] =
    (resourceFragment ++ fr"convert(varchar(50), a.oid, 2) = ${a.getOrElse("")}").query
}

class DoobieCkResourceRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends CkResourceAlgebra[F] {
  import CkResourceSql._

  override def findById(id: Option[String]): OptionT[F, CkResource] =
    OptionT(findByIdSql(id).option.transact(xa))

  override def findByOid(id: Option[String]): OptionT[F, CkResource] =
    OptionT(findByOidSql(id).option.transact(xa))
}

object DoobieCkResourceRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCkResourceRepositoryInterpreter[F] =
    new DoobieCkResourceRepositoryInterpreter(xa)
}
