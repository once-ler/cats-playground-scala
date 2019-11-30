package com.eztier
package epmock

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.extras._

package object domain {
  implicit val epDateCreatedFilterDecoder: Decoder[EpDateCreatedFilter] = deriveDecoder
  implicit val epMatchDecoder: Decoder[EpMatch] = deriveDecoder
  implicit val epProjectDecoder: Decoder[EpProject] = deriveDecoder
  implicit val epQueryDecoder: Decoder[EpQuery] = deriveDecoder

  implicit val epDateCreatedFilterEncoder: Encoder[EpDateCreatedFilter] = deriveEncoder
  implicit val epMatchEncoder: Encoder[EpMatch] = deriveEncoder
  implicit val epProjectEncoder: Encoder[EpProject] = deriveEncoder
  implicit val epQueryEncoder: Encoder[EpQuery] = deriveEncoder
}
