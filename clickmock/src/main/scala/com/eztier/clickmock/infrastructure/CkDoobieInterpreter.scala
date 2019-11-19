package com.eztier.clickmock
package infrastructure

import doobie._
import doobie.implicits._
import cats.data.{NonEmptyList, OptionT}
import cats.effect.{Bracket, IO}
import shapeless._

import domain._

private object Ck_ParticipantSQL {
  def findByIdSql(a: Option[String]): Query0[(Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    sql"""
      select
      convert(varchar(50), a.oid, 2) oid,
      a.class,
      convert(varchar(50), a.extent, 2) extent,
      a._webrUnique_ID,
      convert(varchar(50), a.customAttributes, 2) customAttributes,
      convert(varchar(50), b.oid, 2) oid2,
      b.class class2,
      convert(varchar(50), b.extent, 2) extent2,
      b.medicalRecordNumber,
      convert(varchar(50), b.person, 2) person,
      convert(varchar(50), b.participantCustomExtension, 2) participantCustomExtension
      from __participant a join __participant_customattributesmanager b on b.oid = a.customattributes where _webrunique_id = ${a.getOrElse("")}
    """.query
}

class DoobieCk_ParticipantRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends Ck_ParticipantAlgebra[F] {
  import Ck_ParticipantSQL._

  override def findById(id: Option[String]): OptionT[F, (Ck_Participant, Ck_Participant_CustomAttributesManager)] =
    OptionT(findByIdSql(id).option.transact(xa))
}

object DoobieCk_ParticipantRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieCk_ParticipantRepositoryInterpreter[F] =
    new DoobieCk_ParticipantRepositoryInterpreter(xa)
}
