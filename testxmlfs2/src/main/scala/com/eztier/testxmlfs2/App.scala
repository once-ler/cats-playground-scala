package com.eztier.testxmlfs2

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.eztier.testxmlfs2.openstreetmap.infrastructure.OpenStreetMap
import fs2.{Chunk, Pipe, Pull, Stream, io, text}
import java.nio.file.Paths

import cats.Applicative
import javax.xml.stream.events.Characters

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
    import java.io.{File, FileInputStream, InputStream}
    import java.io.FileReader
    import javax.xml.stream.XMLInputFactory
    import javax.xml.stream.events.{EndElement, StartElement, XMLEvent}
    import javax.xml.stream.XMLStreamException
    import javax.xml.stream.XMLStreamReader

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

    def pipeXMLEventS: Pipe[F, XMLEvent, XMLEvent] = {

      def pullTest(s: Stream[F, XMLEvent]): Pull[F, String, Unit] = {

        s.pull.uncons.flatMap {
          case Some(ev) =>
            ev._1 match {
              case t: StartElement =>
                // println(t.getName.getLocalPart)
                Pull.output(Chunk[String](t.getName.getLocalPart)) >> pullTest(ev._2)
              case e: EndElement =>
                // println(e.getName.getLocalPart)
                Pull.done
              case c: Characters =>
                // println(c.getData)
                Pull.done
              case _ =>
                Pull.done
            }
          case None => Pull.done

        }
      }

      in =>
        // Process inner stream.
        pullTest(in).stream.showLinesStdOut.compile.drain

        in
    }

    s.through(pipeXMLEventS)

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

}

object App extends IOApp {
  val miner = Database.getMiner[IO]

  // def run(args: List[String]): IO[ExitCode] = (OpenStreetMap[IO](miner)).run.as(ExitCode.Success)

  def run(args: List[String]): IO[ExitCode] = (new XmlService[IO]).read2.compile.drain.as(ExitCode.Success)
}

/*
Result:
f
List(<DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Kips Bay, Manhattan Community Board 6, Manhattan, New York County, NYC, New York, 10016, USA</Neighborhood>
</DomainPlace>, <DomainPlace>
  <AddressLine1>550 1st Avenue</AddressLine1>
  <City>New York</City>
  <State>New York</State>
  <ZipCode>10016</ZipCode>
  <Neighborhood>Midtown South, Manhattan Community Board 5, Manhattan, New York County, NYC, New York, 10016, United States of America</Neighborhood>
</DomainPlace>)

 */
