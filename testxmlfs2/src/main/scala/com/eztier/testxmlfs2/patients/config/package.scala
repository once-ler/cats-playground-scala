package com.eztier.testxmlfs2

import com.eztier.testxmlfs2.openstreetmap.infrastructure.{DatabaseConfig, DatabaseConnectionsConfig}
import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val dbconnDecoder: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbDecoder: Decoder[DatabaseConfig] = deriveDecoder
  implicit val appDecoder: Decoder[AppConfig] = deriveDecoder
}