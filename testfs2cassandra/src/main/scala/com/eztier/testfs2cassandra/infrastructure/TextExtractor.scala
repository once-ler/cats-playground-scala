package com.eztier.testfs2cassandra
package infrastructure

import java.util.Date
import java.text.SimpleDateFormat
import java.io.{ByteArrayOutputStream, File, FileInputStream, IOException}

import org.apache.commons.io.FilenameUtils
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.parser.audio.AudioParser
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.image.ImageParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.BodyContentHandler
// import org.xml.sax.helpers.DefaultHandler

import collection.JavaConverters._

import domain.Extracted

sealed trait FileType {
  def getParser: Parser
}
class Text extends FileType {
  override def getParser = new TXTParser()
}
class Html extends FileType {
  override def getParser = new HtmlParser()
}
class Xml extends FileType {
  override def getParser = new XMLParser()
}
class Pdf extends FileType {
  override def getParser = new PDFParser()
}
class Rtf extends FileType {
  override def getParser = new RTFParser()
}
class OOText extends FileType {
  override def getParser = new OpenDocumentParser()
}
class MsExcel extends FileType {
  override def getParser = new OfficeParser()
}
class MsWord extends FileType {
  override def getParser = new OfficeParser()
}
class MsPowerpoint extends FileType {
  override def getParser = new OfficeParser()
}
class MsOutlook extends FileType {
  override def getParser = new OfficeParser()
}
class Visio extends FileType {
  override def getParser = new OfficeParser()
}
class Png extends FileType {
  override def getParser = new ImageParser()
}
class Jpeg extends FileType {
  override def getParser = new ImageParser()
}
class Mp3 extends FileType {
  override def getParser = new AudioParser()
}
class Auto extends FileType {
  override def getParser = new AutoDetectParser()
}

/*
  This class is based on below with some modifications.
  https://github.com/sujitpal/mia-scala-examples/blob/master/src/main/scala/com/mycompany/mia/preprocess/TextExtractor.scala
*/
class TextExtractor {
  private val dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  private val parserContext = new ParseContext()

  private val parsers = Map[String, FileType](
    "text" -> new Text(),
    "html" -> new Html(),
    "htm" -> new Html(),
    "xml" -> new Xml(),
    "pdf" -> new Pdf(),
    "rtf" -> new Rtf(),
    "odt" -> new OOText(),
    "xls" -> new MsExcel(),
    "xlsx" -> new MsExcel(),
    "doc" -> new MsWord(),
    "docx" -> new MsWord(),
    "ppt" -> new MsPowerpoint(),
    "pptx" -> new MsPowerpoint(),
    "pst" -> new MsOutlook(),
    "vsd" -> new Visio(),
    "png" -> new Png(),
    "jpg" -> new Jpeg(),
    "jpeg" -> new Jpeg(),
    "mp3" -> new Mp3(),
    "auto" -> new Auto()
  )

  def detectFileType(file: File): Option[FileType] = {
    val suffix = FilenameUtils.getExtension(file.getName()).toLowerCase()
    if (parsers.contains(suffix)) Some(parsers(suffix)) else None
  }

  private def getStream = {
    val ops = new ByteArrayOutputStream()
    // ops.toByteArray()
  }

  private def aggregateMetadata(metadata: Metadata) =
    metadata.names().foldLeft(Map[String, String]()) {
      (a, n) =>
        a + (n -> metadata.getValues(n).mkString("|"))
    }

  def extract(filePath: String, userAutoParser: Boolean = true): Option[Extracted] = {
    var istream: FileInputStream = null

    val closeQuietly: () => Unit = () => {
      try {
        if (istream != null) {
          istream.close()
        }
      } catch {
        case e: IOException =>
          println(e.getMessage())
      }
    }

    try {
      val file = new File(filePath)
      istream = new FileInputStream(file)
      // val handler = new WriteOutContentHandler(-1)
      val handler = new BodyContentHandler(-1)
      // val handler = new DefaultHandler()
      val metadata = new Metadata()
      val maybeParser = if (userAutoParser) Some(parsers("auto")) else detectFileType(file)

      maybeParser match {
        case Some(parser) =>
          // val tessearactConfig = new TesseractOCRConfig()
          // parserContext.set(classOf[TesseractOCRConfig], tessearactConfig)
          parser.getParser.parse(istream, handler, metadata, parserContext)

          val aggMeta = aggregateMetadata(metadata)
          val content = handler.toString()

          closeQuietly()

          Some(
            Extracted(
              doc_file_path = Some(filePath),
              content = Some(content),
              metadata = Some(aggMeta)
            )
          )
        case _ => None
      }
    } catch {
      case e: Exception =>
        println(e.getMessage())

        closeQuietly()

        None
    }
  }
}

object TextExtractor {
  def apply() = new TextExtractor
}
