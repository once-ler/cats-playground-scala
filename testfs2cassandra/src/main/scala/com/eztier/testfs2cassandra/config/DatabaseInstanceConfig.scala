package com.eztier.testfs2cassandra
package config

case class DatabaseInstanceConfig
(
  local: DatabaseConfig,
  eventstore: DatabaseConfig
)
