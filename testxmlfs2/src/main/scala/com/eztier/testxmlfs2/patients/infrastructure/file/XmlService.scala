package com.eztier.testxmlfs2
package patients.infrastructure.file

import java.nio.file.Paths

import cats.implicits._
import cats.Show
import cats.effect.{Blocker, ContextShift, Sync}
import com.eztier.testxmlfs2.Util.filterLeft
import com.eztier.testxmlfs2.patients.domain.Patient
import fs2.{Pipe, Stream, io, text}

import scala.xml.Elem

class XmlService[F[_]: Sync : ContextShift] {
  val workingDir = System.getProperty("user.dir")
  val filePath = s"$workingDir/testxmlfs2/src/main/resources/patients.xml"

  def read: Stream[F, Unit] = Stream.resource(Blocker[F]).flatMap { blocker =>
    io.file.readAll[F](Paths.get(filePath), blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .showLinesStdOut
  }

  def read2 = {
    import java.io.{File, FileInputStream}
    import java.io.FileReader
    import javax.xml.stream.XMLInputFactory
    import javax.xml.stream.events.{XMLEvent}

    val inputFactory = XMLInputFactory.newInstance()

    val fileReader = new FileReader(filePath)
    def fileAsInputStream = new FileInputStream(new File(filePath))

    val xmlEventReader = inputFactory.createXMLEventReader(fileAsInputStream)

    val s: Stream[F, XMLEvent] = Stream.unfold(xmlEventReader){
      a =>
        if (a.hasNext())
          Some(a.nextEvent(), a)
        else
          None
    }

    implicit val showXMLEvent: Show[XMLEvent] = a => a.isStartElement().toString()

    s.showLinesStdOut

    /* java iterator style.
    while (xmlEventReader.hasNext()) {

      val evType = xmlEventReader.nextEvent()

      evType match {
        case t: StartElement =>
          println(t.getName.getLocalPart)
        case e: EndElement =>
          println(e.getName.getLocalPart)
        case c: Characters =>
          println(c.getData)
        case _ =>
      }
    }
    xmlEventReader.close()

    Stream.eval(Applicative[F].pure(()))
    */

  }

  def fetchPatients = {
    import com.scalawilliam.xs4s.XmlElementExtractor
    import com.scalawilliam.xs4s.Implicits._

    import java.io.{File, FileInputStream}
    import java.io.FileReader
    import javax.xml.stream.XMLInputFactory

    import kantan.xpath._
    import kantan.xpath.implicits._
    import kantan.xpath.java8._ // LocalDateTime

    val inputFactory = XMLInputFactory.newInstance()

    val fileReader = new FileReader(filePath)
    def fileAsInputStream = new FileInputStream(new File(filePath))

    val xmlEventReader = inputFactory.createXMLEventReader(fileAsInputStream)

    val splitter = XmlElementExtractor.collectElements(_.last == "patient")

    /*
    val li = for {
      el <- xmlEventReader.toIterator.scanCollect(splitter.Scan)
    } yield el

    li.foreach(a => println(a.toString))
    */

    implicit val placeDecoder: NodeDecoder[Patient] = NodeDecoder.decoder(
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
    )(Patient.apply)

    val elemToStringPipeS: Pipe[F, Elem, String] = _.map {
      in =>
        in.toString()
    }

    def stringToPatient: Pipe[F, String, Either[XPathError, List[Patient]]] = _.map {
      str =>
        val result: kantan.xpath.XPathResult[List[Patient]] = str.evalXPath[List[Patient]](xp"/patient")
        result
    }

    implicit val showPatient: Show[Patient] = a => s"${a.PatientName}"

    implicit val showXPathError: Show[kantan.xpath.XPathError] = a => a.getMessage

    Stream.fromIterator(xmlEventReader.toIterator.scanCollect(splitter.Scan))
      .through(elemToStringPipeS)
      .through(stringToPatient)
      .through(filterLeft)
      .flatMap(Stream.emits)
  }

}
