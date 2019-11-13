package com.eztier.testxmlfs2
package patients.domain

case class PhoneNumberHome
(
  telephoneNumber: Option[String] = None,
  telecommunicationUseCode: Option[String] = None,
  telecommunicationEquipmentType: Option[String] = None,
  emailAddress: Option[String] = None,
  countryCode: Option[String] = None,
  areaCode: Option[String] = None,
  localNumber: Option[String] = None,
  extension: Option[String] = None,
  anyText: Option[String] = None
)
