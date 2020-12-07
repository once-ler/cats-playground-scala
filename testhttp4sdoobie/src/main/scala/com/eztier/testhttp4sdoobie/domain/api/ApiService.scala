package com.eztier
package testhttp4sdoobie
package domain
package api

import cats.data.Chain
import cats.{Functor, Monad}
import fs2.Stream
import io.circe.{Decoder, Encoder, Json}
import org.http4s.Headers
import org.http4s.client.Client

import common.MonadLog
import config.HttpConfig

class ApiService[F[_]: Functor: Monad](repository: ApiAlgebra[F])(implicit ev: MonadLog[F, Chain[String]]) {
  
  val logs = implicitly[MonadLog[F, Chain[String]]]

  def importData[A](data: A, options: Chain[(String, String)], headers: Headers = Headers.empty)(implicit ev: Encoder[A]): Stream[F, ApiResp] =
    repository.importData[A](data, options)

  def exportData[A](options: Chain[(String, String)])(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]] =
    repository.exportData(options)

  def exportData(url: String, headers: Headers = Headers.empty): Stream[F, Json] =
    repository.exportData(url, headers)
    
  def exportDataT[A](url: String, headers: Headers = Headers.empty)(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]] =
    repository.exportDataT(url, headers)

  def readAllFromFile(path: String, bufferSize: Int = 8192): F[String] =
    repository.readAllFromFile(path, bufferSize)

  def readByLinesFromFile(path: String, bufferSize: Int = 8192): Stream[F, String] =
    repository.readByLinesFromFile(path, bufferSize)

  def showLog: F[String] =
    repository.showLog

  def showConf: F[HttpConfig] =
    repository.showConf
}

object ApiService {
  def apply[F[_]: Functor: Monad](repository: ApiAlgebra[F])(implicit ev: MonadLog[F, Chain[String]]): ApiService[F] =
    new ApiService[F](repository)
}

