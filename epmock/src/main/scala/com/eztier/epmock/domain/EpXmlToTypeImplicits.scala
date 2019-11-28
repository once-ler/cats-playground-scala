package com.eztier
package epmock.domain

import cats.Show
import cats.effect.{ContextShift, Sync}
import fs2.{Pipe, Stream}
import javax.xml.stream.XMLEventReader
import scala.xml.Elem

import common.Util._

object EpXmlToTypeImplicits {
  def xmlEventToEpPatientStream[F[_]: Sync : ContextShift](xmlEventReader: XMLEventReader) = {
    import com.scalawilliam.xs4s.XmlElementExtractor
    import com.scalawilliam.xs4s.Implicits._

    import java.io.{File, FileInputStream}
    import java.io.FileReader
    import javax.xml.stream.XMLInputFactory

    import kantan.xpath._
    import kantan.xpath.implicits._
    import kantan.xpath.java8._ // LocalDateTime

    val splitter = XmlElementExtractor.collectElements(_.last == "patient")

    implicit val placeDecoder: NodeDecoder[EpPatient] = NodeDecoder.decoder(
      xp"./AdministrativeSex/text()",
      xp"./DateTimeofBirth/text()",
      xp"./EthnicGroup/text()",
      xp"./PatientAddress/text()",
      xp"./PatientName/text()",
      xp"./PhoneNumberHome/text()",
      xp"./Race/text()",
      xp"./_id/text()",
      xp"./dateCreated/text()",
      xp"./dateLocal/text()"
    )(EpPatient.apply)

    val elemToStringPipeS: Pipe[F, Elem, String] = _.map {
      in =>
        in.toString()
    }

    def stringToPatient: Pipe[F, String, Either[XPathError, List[EpPatient]]] = _.map {
      str =>
        val result: kantan.xpath.XPathResult[List[EpPatient]] = str.evalXPath[List[EpPatient]](xp"/patient")
        result
    }

    implicit val showPatient: Show[EpPatient] = a => s"${a.PatientName}"

    implicit val showXPathError: Show[kantan.xpath.XPathError] = a => a.getMessage

    Stream.fromIterator(xmlEventReader.toIterator.scanCollect(splitter.Scan))
      .through(elemToStringPipeS)
      .through(stringToPatient)
      .through(filterLeft)
      .flatMap(Stream.emits)
  }
}
