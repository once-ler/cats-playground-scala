package com.eztier
package epmock

import io.circe.{Decoder, Encoder, Json}
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
  implicit val epQueryEncoder: Encoder[EpQuery] = Encoder.forProduct2("match", "project")(b => (b.Match, b.project))
}
