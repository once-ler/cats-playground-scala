package com.eztier.testxmlfs2
package patients.domain

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