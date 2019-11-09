package com.eztier.testxmlfs2
package patients.domain

import java.time.LocalDateTime

case class Patient
(
  AdministrativeSex: Option[String],
  DateTimeofBirth: Option[String],
  EthnicGroup: Option[String],
  PatientAddress: Option[String],
  PatientName: Option[String],
  PhoneNumberHome: Option[String],
  Race: Option[String],
  Mrn: Option[String],
  dateCreated: Option[Long],
  dateLocal: Option[String]
)
