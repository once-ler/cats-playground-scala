package com.eztier.testfs2cassandra
package config

case class CassandraConnectionConfig
(
  host: String,
  port: Int,
  user: Option[String] = None,
  password: Option[String] = None
)

case class CassandraConfig
(
  connection: CassandraConnectionConfig
)
