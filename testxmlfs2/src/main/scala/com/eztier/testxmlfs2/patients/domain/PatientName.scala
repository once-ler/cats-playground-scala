package com.eztier.testxmlfs2
package patients.domain

case class PatientName
(
  lastName: Option[String] = None,
  firstName: Option[String] = None,
  middleName: Option[String] = None,
  suffix: Option[String] = None,
  prefix: Option[String] = None,
  degree: Option[String] = None,
  nameTypeCode: Option[String] = None,
  nameRepresentationCode: Option[String] = None
)
