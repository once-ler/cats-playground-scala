package com.eztier.clickmock
package domain

import cats.{Applicative, Functor, Monad}
import cats.effect.{Async, Concurrent}
import fs2.Pipe

import scala.xml.NodeSeq
import cats.data.EitherT
import CkMergeTypeImplicits._
import cats.implicits._

class Ck_ParticipantAggregator[F[_]: Applicative: Async: Concurrent](
  entityService: CkEntityService[F],
  participantService: Ck_ParticipantService[F],
  participantCustomExtensionService: Ck_ParticipantCustomExtensionService[F],
  personService: CkPersonService[F],
  personCustomExtensionService: Ck_PersonCustomExtensionService[F],
  partyService: CkPartyService[F],
  resourceService: CkResourceService[F]) {

  val getParticipantService: Ck_ParticipantService[F] = participantService

  // Should log error when folding.
  def getParticipant(id: Option[String]): F[CkParticipantAggregate] =
    for {
      pa <- participantService
        .findById(id)
        .fold(e => (Ck_Participant(), Ck_Participant_CustomAttributesManager()), a => a)
      pac <- participantCustomExtensionService
        .findByOid(pa._2.participantCustomExtension.getOrElse(EntityReference[Ck_ParticipantCustomExtension]()).Poref)
        .fold(e => (Ck_ParticipantCustomExtension(), Ck_ParticipantCustomExtension_CustomAttributesManager()), a => a)
      pe <- personService
        .findByOid(pa._2.person.getOrElse(EntityReference[CkPerson]()).Poref)
        .fold(e => (CkPerson(), CkPerson_CustomAttributesManager()), a => a)
      pec <- personCustomExtensionService
        .findByOid(pe._2.personCustomExtension.getOrElse(EntityReference[Ck_PersonCustomExtension]()).Poref)
        .fold(e => (Ck_PersonCustomExtension(), Ck_PersonCustomExtension_CustomAttributesManager()), a => a)
      pt <- partyService
        .findByOid(pe._1.oid)
        .fold(e => (None, None, None, None, None), a => a)
      r <- resourceService
        .findByOid(pa._1.oid)
        .fold(e => CkResource(), a => a)
    } yield CkParticipantAggregate(
      participant = Some(pa._1),
      participantCm = Some(pa._2),
      participantExtension = Some(pac._1),
      participantExtensionCm = Some(pac._2),
      person = Some(pe._1),
      personCm = Some(pe._2),
      personExtension = Some(pec._1),
      personExtensionCm = Some(pec._2),
      party = pt._1,
      partyContactInformation = pt._2,
      phoneContactInformation = pt._3,
      emailContactInformation = pt._4,
      postalContactInformation = pt._5,
      resource = Some(r)
    )

  private def addOrUpdateImpl[A <: CkBase with WithCustomAttributes with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B): F[NodeSeq] = {
    fromCk.oid match {
      case null | Some("") =>
        // Does root object with mrn exist?
        val e: EitherT[F, String, Nothing] = EitherT.leftT("Not found")

        val fa = fromCk.toCkTypeName match {
          case a if a == "_Participant" => participantService.findById(Some(mrn))
          case a if a == "_ParticipantCustomExtension" => participantCustomExtensionService.findById(Some(mrn))
          case a if a == "Person" => personService.findById(Some(mrn))
          case a if a == "_PersonCustomExtension" => personCustomExtensionService.findById(Some(mrn))
          case _ => e
        }

        val fb = fa.fold(
          e => {
            entityService.createCompleteEntity(fromCaCm, fromCa)
          }
          , c => {
            val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
            val fromCa1 = maybeMerge(fromCa, c._1.asInstanceOf[A], fromCaCm1)
            entityService.redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
          }
        )

        Monad[F].flatMap(fb)(fc => fc)

      case _ =>
        // Object already exists, but may or may not have the mrn as its ID.
        val fromCaCm1 = maybeMerge(fromCaCm, fromCkCm)
        val fromCa1 = maybeMerge(fromCa, fromCk, fromCaCm1)
        entityService.redefineCompleteEntity(fromCa1.asInstanceOf[A], fromCaCm1.asInstanceOf[B])
    }
  }

  def addOrUpdateNonProject[A <: CkBase with WithCustomAttributes with WithNonProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B): F[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateProject[A <: CkBase with WithCustomAttributes with WithProject with WithEncoder with WithFindById, B <: CkBase with WithEncoder](mrn: String = "", fromCk: A, fromCa: A, fromCkCm: B, fromCaCm: B): F[NodeSeq] = {
    addOrUpdateImpl(mrn, fromCk, fromCa, fromCkCm, fromCaCm)
  }

  def addOrUpdateEntity[A <: CkBase with WithEncoder with WithExplicitTypeName](in: A): F[NodeSeq] = {
    in.oid match {
      case null | Some("") => entityService.createEntity(in)
      case _ => entityService.redefineEntity(in)
    }
  }

  def addOrCreateEntityFlow[A <: CkBase with WithEncoder with WithExplicitTypeName]: Pipe[F, A, NodeSeq] = _.evalMap {
    a =>
      a match {
        case a if a.getClass == classOf[CkPostalContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkPostalContactInformation])
        case a if a.getClass == classOf[CkEmailContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkEmailContactInformation])
        case a if a.getClass == classOf[CkPhoneContactInformation] => addOrUpdateEntity(a.asInstanceOf[CkPhoneContactInformation])
        case _ => Applicative[F].pure(NodeSeq.Empty)
      }
  }
}

object Ck_ParticipantAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](
    entityService: CkEntityService[F],
    participantService: Ck_ParticipantService[F],
    participantCustomExtensionService: Ck_ParticipantCustomExtensionService[F],
    personService: CkPersonService[F],
    personCustomExtensionService: Ck_PersonCustomExtensionService[F],
    partyService: CkPartyService[F],
    resourceService: CkResourceService[F]): Ck_ParticipantAggregator[F] =
    new Ck_ParticipantAggregator[F](entityService, participantService, participantCustomExtensionService, personService, personCustomExtensionService, partyService, resourceService)
}
