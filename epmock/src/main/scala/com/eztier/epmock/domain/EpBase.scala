package com.eztier.epmock
package domain

import io.circe.generic.extras.Configuration

case class EpPatient
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

case class Ethnicity
(
  ethnicity1: Option[String] = None,
  ethnicity2: Option[String] =  None,
  ethnicity3: Option[String] = None
)

case class Race
(
  race1: Option[String] = None,
  race2: Option[String] =  None,
  race3: Option[String] = None
)

case class PatientAddress
(
  streetAddress: Option[String] = None,
  otherDesignation: Option[String] = None,
  city: Option[String] = None,
  stateOrProvince: Option[String] = None,
  zipOrPostalCode: Option[String] = None,
  country: Option[String] = None,
  addressType: Option[String] = None,
  otherGeographicDesignation: Option[String] = None,
  countyParishCode: Option[String] = None,
  censusTract: Option[String] = None,
  addressRepresentationCode: Option[String] = None
)

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

// For sending json http reuqest.
case class EpDateCreatedFilter
(
  $gte: String = ""
)

case class EpMatch
(
  dateCreated: EpDateCreatedFilter = EpDateCreatedFilter()
)

case class EpProject
(
  dateCreated: Int = 1,
  dateLocal: Int = 1,
  Race: Int = 1,
  EthnicGroup: Int = 1,
  PatientName: Int = 1,
  DateTimeofBirth: Int = 1,
  AdministrativeSex: Int = 1,
  PatientAddress: Int = 1,
  PhoneNumberHome: Int = 1
)

case class EpQuery
(
  Match: EpMatch = EpMatch(),
  Project: EpProject = EpProject()
)

