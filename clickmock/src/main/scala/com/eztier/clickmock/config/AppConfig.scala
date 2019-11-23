package com.eztier.clickmock
package config

final case class AppConfig
(
  soap: SoapConfig,
  db: DatabaseInstanceConfig
)