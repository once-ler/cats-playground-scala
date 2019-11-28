package com.eztier.epmock
package infrastructure.file

import java.io.{File, FileInputStream}
import cats.effect.{ContextShift, Sync}
import fs2.Stream
import javax.xml.stream.{XMLEventReader, XMLInputFactory}

import domain._

class FilePatientRepositoryInterpreter[F[_]: Sync : ContextShift] extends EpPatientRepositoryAlgebra[F] {
  val workingDir = System.getProperty("user.dir")
  val filePath = s"$workingDir/testxmlfs2/src/main/resources/patients.xml"

  override def fetchPatients: Stream[F, EpPatient] = {
    val inputFactory = XMLInputFactory.newInstance()

    def fileAsInputStream = new FileInputStream(new File(filePath))

    val xmlEventReader: XMLEventReader = inputFactory.createXMLEventReader(fileAsInputStream)

    EpXmlToTypeImplicits.xmlEventToEpPatientStream(xmlEventReader)
  }

  override def insertMany(a: List[EpPatient]): F[Int] = ???

  override def list(): F[List[EpPatient]] = ???

  override def truncate(): F[Int] = ???
}
