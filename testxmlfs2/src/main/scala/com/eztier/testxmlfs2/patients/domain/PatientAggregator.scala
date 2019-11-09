package com.eztier.testxmlfs2
package patients.domain

import com.eztier.testxmlfs2.patients.infrastructure.file.XmlService

class PatientAggregator[F[_]](patientService: PatientService[F], xmlService: XmlService[F]) {
  def run = {
    xmlService.read3
  }
}

object PatientAggregator {
  def apply[F[_]](patientService: PatientService[F], xmlService: XmlService[F]): PatientAggregator[F] =
    new PatientAggregator[F](patientService, xmlService)
}