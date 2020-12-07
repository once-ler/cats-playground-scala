package com.eztier.testhttp4sdoobie
package domain
package api

import io.circe.Json

sealed trait ApiResp
case class ApiError(body: Json, error: String) extends ApiResp
case class ApiOk(body: Json) extends ApiResp
