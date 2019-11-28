package com.eztier
package epmock
package infrastructure.http

import cats.effect.{ConcurrentEffect, ContextShift}
import fs2.{Pipe, Stream}
import kantan.xpath._
import kantan.xpath.implicits._
import org.http4s.{Header, Method, Request, Uri}
import common.Util._
import domain._
import domain.EpXmlToTypeImplicits._

class HttpPatientRepositoryInterpreter[F[_]: ConcurrentEffect: ContextShift[?[_]]]
  extends WithBlockingEcStream with EpPatientRepositoryAlgebra[F]{

  val moreHeaders = headers.put(Header("Content-Type", "text/xml"))

  def createRequest(zipCode: String = ""): Request[F] = Request[F](
    method = Method.POST,
    uri = Uri.unsafeFromString("https://www.w3schools.com/xml/tempconvert.asmx"),
    headers = moreHeaders,
    body = strBody(
      """<?xml version="1.0" encoding="utf-8"?>
        |<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        |  <soap:Body>
        |    <FahrenheitToCelsius xmlns="https://www.w3schools.com/xml/">
        |      <Fahrenheit>75</Fahrenheit>
        |    </FahrenheitToCelsius>
        |  </soap:Body>
        |</soap:Envelope>
      """.stripMargin)
  )

  def patientPipeS[F[_]]: Pipe[F, String, Either[XPathError, List[EpPatient]]] = _.map {
    str =>
      val result: kantan.xpath.XPathResult[List[EpPatient]] = str.evalXPath[List[EpPatient]](xp"//patient")
      result
  }

  override def fetchPatients(): Stream[F, EpPatient] =
    clientBodyStream()
      .filter(_.length > 0)
      .through(patientPipeS)
      .through(filterLeft)
      .flatMap(Stream.emits)

  override def insertMany(a: List[EpPatient]): F[Int] = ???

  override def list(): F[List[EpPatient]] = ???

  override def truncate(): F[Int] = ???
}
