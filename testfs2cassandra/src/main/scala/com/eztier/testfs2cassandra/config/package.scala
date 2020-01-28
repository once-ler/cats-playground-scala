package com.eztier
package testfs2cassandra

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object config {
  implicit val appDecoder: Decoder[AppConfig] = deriveDecoder
  implicit val dbconnDec: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbInstanceDec: Decoder[DatabaseInstanceConfig] = deriveDecoder
  implicit val dbDec: Decoder[DatabaseConfig] = deriveDecoder
  implicit val httpInstanceDec: Decoder[HttpInstanceConfig] = deriveDecoder
  implicit val httpDec: Decoder[HttpConfig] = deriveDecoder
  implicit val cassandraConnectionDec: Decoder[CassandraConnectionConfig] = deriveDecoder
  implicit val cassandraDec: Decoder[CassandraConfig] = deriveDecoder
  implicit val textExtractorDec: Decoder[TextExtractorConfig] = deriveDecoder

  implicit val appEncoder: Encoder[AppConfig] = deriveEncoder
  implicit val dbconnEnc: Encoder[DatabaseConnectionsConfig] = deriveEncoder
  implicit val dbInstanceEnc: Encoder[DatabaseInstanceConfig] = deriveEncoder
  implicit val dbEnc: Encoder[DatabaseConfig] = deriveEncoder
  implicit val httpInstanceEnc: Encoder[HttpInstanceConfig] = deriveEncoder
  implicit val httpEnc: Encoder[HttpConfig] = deriveEncoder
  implicit val cassandraConnectionEnc: Encoder[CassandraConnectionConfig] = deriveEncoder
  implicit val cassandraEnc: Encoder[CassandraConfig] = deriveEncoder
  implicit val textExtractorEnc: Encoder[TextExtractorConfig] = deriveEncoder
}
