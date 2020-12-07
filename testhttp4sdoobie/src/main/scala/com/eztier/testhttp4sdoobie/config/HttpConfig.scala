package com.eztier.testhttp4sdoobie
package config

case class HttpConfig
(
  url: String,
  token: Option[String],
  odm: Option[String]
)
