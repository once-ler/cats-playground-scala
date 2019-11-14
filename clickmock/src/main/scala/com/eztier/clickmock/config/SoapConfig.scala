package com.eztier.clickmock
package config

final case class SoapConfig
(
  url: Option[String],
  store: Option[String],
  user: Option[String],
  pass: Option[String]
)
