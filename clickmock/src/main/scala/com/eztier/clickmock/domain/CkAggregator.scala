package com.eztier.clickmock
package domain

import cats.Applicative
import cats.effect.{Async, Concurrent}

class Ck_ParticipantAggregator[F[_]: Applicative: Async: Concurrent](participantService: Ck_ParticipantService[F], personService: CkPersonService[F], personCustomExtensionService: Ck_PersonCustomExtensionService[F]) {
  def getParticipant(id: Option[String]) =
    participantService
      .findById(id).fold(e => (Ck_Participant(), Ck_Participant_CustomAttributesManager()), a => a)
}

object Ck_ParticipantAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](participantService: Ck_ParticipantService[F], personService: CkPersonService[F], personCustomExtensionService: Ck_PersonCustomExtensionService[F]): Ck_ParticipantAggregator[F] =
    new Ck_ParticipantAggregator[F](participantService, personService, personCustomExtensionService)
}