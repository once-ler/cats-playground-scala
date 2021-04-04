package com.eztier
package testhl7.tagless.multi
package Ep

import java.time.Instant
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}
import cats.implicits._
import cats.data.{Chain, EitherT}
import cats.{Applicative, Functor, Monad, SemigroupK}
import cats.effect.{Async, Concurrent, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import fs2.{Pipe, Stream}
import fs2.text.{utf8DecodeC, utf8Encode}
// import algae.createMonadLog
// import algae.mtl.MonadLog
import com.eztier.common.MonadLog

import io.chrisdavenport.log4cats.Logger
import kantan.xpath._
import kantan.xpath.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityBody, Header, Headers, Method, Request, Uri}
import common.CatsLogger._
import common.Util._
import Ck.Domain._

object Package {
  import Domain._
  import Infrastucture._

  def createEpPatientAggregatorResource[F[_]: Async :ContextShift :ConcurrentEffect: Timer] =
    for {
      // implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(createMonadLog[F, Chain[String]])
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(MonadLog.createMonadLog[F, String])
      patientRepo = HttpPatientRepositoryInterpreter[F]
      patientService = EpPatientService(patientRepo)
      epPatientAggregator = new EpPatientAggregator[F](patientService)
    } yield epPatientAggregator
}

object EpXmlToTypeImplicits {

  import Domain._
  import kantan.xpath._
  import kantan.xpath.implicits._
  import kantan.xpath.java8._ // LocalDateTime

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
}

object Domain {
  case class EpPatient
  (
    AdministrativeSex: Option[String] = None,
    DateTimeofBirth: Option[String] = None,
    EthnicGroup: Option[String] = None,
    PatientAddress: Option[String] = None,
    PatientName: Option[String] = None,
    PhoneNumberHome: Option[String] = None,
    Race: Option[String] = None,
    Mrn: Option[String] = None,
    dateCreated: Option[Long] = None,
    dateLocal: Option[String] = None
  )

  class EpPatientAggregator[F[_]: Applicative: Async: Concurrent: Monad : MonadLog[?[_], Chain[String]]](patientService: EpPatientService[F]) {
    val logs = implicitly[MonadLog[F, Chain[String]]]

    def getOrCreateEntity(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]): F[CkParticipantAggregate] =
      for {
        x0 <- ckEntityAggregator.getOrCreate(oid)
        a: GetEntityByIDResponse = WrappedEntityXml(x0)
        x1 <- ckEntityAggregator.getOrCreate(oid)
        b: GetEntityByIDResponse = WrappedEntityXml(x1)
        x2 <- ckEntityAggregator.getOrCreate(oid)
        c: GetEntityByIDResponse = WrappedEntityXml(x2)
        x3 <- ckEntityAggregator.getOrCreate(oid)
        d: GetEntityByIDResponse = WrappedEntityXml(x3)
      } yield CkParticipantAggregate(
        entityA = a.some,
        entityB = b.some,
        entityC = c.some,
        entityD = d.some
      )

    def getOrCreateEntityF(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]): F[CkParticipantAggregate] =
      for {
        x0 <- ckEntityAggregator.getOrCreateF(oid)
        a: Option[GetEntityByIDResponse] = x0 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x1 <- ckEntityAggregator.getOrCreateF(oid)
        b: Option[GetEntityByIDResponse] = x1 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x2 <- ckEntityAggregator.getOrCreateF(oid)
        c: Option[GetEntityByIDResponse] = x2 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        x3 <- ckEntityAggregator.getOrCreateF(oid)
        d: Option[GetEntityByIDResponse] = x3 match {
          case Right(d) =>
            val x: GetEntityByIDResponse = WrappedEntityXml(d)
            Some(x)
          case _ => None
        }
        l <- logs combineK ckEntityAggregator.logs
        // l <- logs.get
        _ <- Logger[F].error(l.show) // Write out all the accumulated errors.
      } yield CkParticipantAggregate(
        entityA = a,
        entityB = b,
        entityC = c,
        entityD = d
      )

    def getMaxDateProcessed: F[Instant] =
      Instant.now().pure[F]

    def toHttpRequestPipeS: Pipe[F, Instant, List[EpPatient]] = _.evalMap {
      in =>
        patientService.fetchPatients(in.some).compile.toList
    }

    def toDumpMonadLogPipeS: Pipe[F, List[EpPatient], List[EpPatient]] = _.evalMap {
      in =>
        for {
          l <- logs.get
          _ <- Logger[F].error(l.show)
        } yield in
    }

    def run =
      Stream.eval(getMaxDateProcessed)
        .through(toHttpRequestPipeS)
        .through(toDumpMonadLogPipeS)

    def runUnprocessed(ckEntityAggregator: CkEntityAggregator[F], oid: Option[String]) =
      Stream.eval(getOrCreateEntityF(ckEntityAggregator, oid))
  }

  trait EpPatientRepositoryAlgebra[F[_]] {
    def fetchPatients(maxDateProcessed: Option[Instant] = None): Stream[F, EpPatient]
  }

  class EpPatientService[F[_]: Functor: Monad : MonadLog[?[_], Chain[String]]](repository: EpPatientRepositoryAlgebra[F]) {
    val logs = implicitly[MonadLog[F, Chain[String]]]

    def fetchPatients(maxDateProcessed: Option[Instant] = None): Stream[F, EpPatient] =
      repository.fetchPatients(maxDateProcessed)
  }

  object EpPatientService {
    def apply[F[_]: Functor: Monad : MonadLog[?[_], Chain[String]]](repository: EpPatientRepositoryAlgebra[F]): EpPatientService[F] =
      new EpPatientService[F](repository)
  }
}

object Infrastucture {
  import Domain._
  import EpXmlToTypeImplicits._

  abstract class WithBlockingEcStream[F[_]: ConcurrentEffect] {

    // Don't block the main thread
    def blockingEcStream: Stream[F, ExecutionContext] =
      Stream.bracket(Sync[F].delay(Executors.newFixedThreadPool(4)))(pool =>
        Sync[F].delay(pool.shutdown()))
        .map(ExecutionContext.fromExecutorService)
  }

  class HttpPatientRepositoryInterpreter[F[_]: Functor: ConcurrentEffect: ContextShift[?[_]] : MonadLog[?[_], Chain[String]]]
    extends WithBlockingEcStream with EpPatientRepositoryAlgebra[F] {

    val logs = implicitly[MonadLog[F, Chain[String]]]

    val headers = Headers.of(Header("Accept", "*/*"))

    def getBody(body: EntityBody[F]): F[Vector[Byte]] = body.compile.toVector

    def strBody(body: String): EntityBody[F] = fs2.Stream(body).through(utf8Encode)

    val moreHeaders = headers.put(Header("Content-Type", "text/xml"))

    def createRequest(lastDateProcessed: Option[Instant] = None): Request[F] =
      Request[F](
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

    def clientBodyStream(lastDateProcessed: Option[Instant] = None): Stream[F, String] =
      blockingEcStream.flatMap {
        ec =>
          for {
            client <- BlazeClientBuilder[F](ec).stream
            plainRequest <- Stream.eval[F, Request[F]](Applicative[F].pure[Request[F]](createRequest(lastDateProcessed)))
            entityBody <- client.stream(plainRequest).flatMap(_.body.chunks).through(utf8DecodeC) // def utf8DecodeC[F[_]]: Pipe[F, Chunk[Byte], String]
          } yield entityBody
      }.handleErrorWith {
        e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.eval(logs.log(Chain.one(s"${ex}")))
            .flatMap(a => Stream.emit(ex))
      }

    private def toDumpResponsePipeS[F[_]: Applicative: Logger]: Pipe[F, String, String] = _.evalMap {
      str =>
        Logger[F].error(str) *> Applicative[F].pure(str)
    }

    def patientPipeS: Pipe[F, String, Either[XPathError, List[EpPatient]]] = _.evalMap {
      str =>
        val result: kantan.xpath.XPathResult[List[EpPatient]] = str.evalXPath[List[EpPatient]](xp"//patient")

        result match {
          case Left(e) =>
            val ex = WrapThrowable(e).printStackTraceAsString
            val a: F[Either[XPathError, List[EpPatient]]] = for {
              _ <- logs.log(Chain(ex))
              r1: Either[XPathError, List[EpPatient]] = Left(e)
            } yield r1
            a

          case Right(a) =>
            val r1: Either[XPathError, List[EpPatient]] = Right(a)
            r1.pure[F]
        }
    }

    override def fetchPatients(maxDateProcessed: Option[Instant]): Stream[F, EpPatient] = {
      val fa = clientBodyStream(maxDateProcessed)
        .filter(_.length > 0)
        .through(toDumpResponsePipeS)
        .compile
        .toVector
        .flatMap(b => s"<patients>${b.mkString("")}</patients>".pure[F])

      Stream.eval(fa)
        .through(patientPipeS)
        .through(filterLeft)
        .flatMap(Stream.emits)
    }
  }

  object HttpPatientRepositoryInterpreter {
    def apply[F[_]: Functor: ConcurrentEffect: ContextShift[?[_]] : MonadLog[?[_], Chain[String]]]: HttpPatientRepositoryInterpreter[F] = new HttpPatientRepositoryInterpreter[F]
  }

}
