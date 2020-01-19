package com.eztier.testfs2cassandra
package config

final case class AppConfig
(
  http: HttpInstanceConfig,
  db: DatabaseInstanceConfig
)