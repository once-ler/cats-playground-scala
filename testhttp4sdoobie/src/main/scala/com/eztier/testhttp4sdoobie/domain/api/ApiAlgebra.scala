package com.eztier.testhttp4sdoobie
package domain
package api

import cats.data.Chain
import fs2.Stream
import io.circe.{Decoder, Encoder, Json}
import org.http4s._
import org.http4s.client.Client
import org.http4s.Headers

import config.HttpConfig

trait ApiAlgebra[F[_]] {
  def importData[A](data: A, options: Chain[(String, String)], headers: Headers = Headers.empty)(implicit ev: Encoder[A]): Stream[F, ApiResp]
  def exportData[A](options: Chain[(String, String)])(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]]
  def exportData(url: String, headers: Headers = Headers.empty): Stream[F, Json]
  def exportDataT[A](url: String, headers: Headers = Headers.empty)(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]]
  def readAllFromFile(path: String, bufferSize: Int = 8192): F[String]
  def readByLinesFromFile(path: String, bufferSize: Int = 8192): Stream[F, String]
  def showLog: F[String]
  def showConf: F[HttpConfig]  
}
