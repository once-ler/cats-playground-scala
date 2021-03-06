package com.eztier
package testhttp4sdoobie
package infrastructure
package http

import javax.net.ssl.SSLContext
  
import cats.data.Chain
import cats.{Applicative, Functor}
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Sync, Resource}
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.text.{utf8Decode, utf8DecodeC, utf8Encode, lines}

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.optics.JsonPath._
import java.nio.file.Paths
import java.util.concurrent.Executors

import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
// import org.http4s.client.okhttp.OkHttpBuilder

import scala.concurrent.ExecutionContext

import common.{MonadLog, _}
import common.Util._
import domain._
import domain.api._
import com.eztier.testhttp4sdoobie.config.HttpConfig

class HttpInterpreter[F[_]: Functor: ConcurrentEffect: ContextShift[?[_]]]
  (conf: HttpConfig)(implicit logs: MonadLog[F, Chain[String]])
  extends ApiAlgebra[F] {

  private val pool = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
  private val client = BlazeClientBuilder(pool)
  private val blocker = Blocker.liftExecutionContext(pool)

  val headers = Headers.of(Header("Accept", "*/*"))

  def getBody(body: EntityBody[F]): F[Vector[Byte]] = body.compile.toVector

  def strBody(body: String): EntityBody[F] = fs2.Stream(body).through(utf8Encode)

  // val moreHeaders = headers.put(Header("Content-Type", "application/x-www-form-urlencoded"))

  private def defaultRequestBody: Chain[(String, String)] = Chain(
    "token" -> conf.token.getOrElse(""),
    "format" -> "json",
    "type" -> "flat"
  )

  def clientBodyStream(request: Request[F]): Stream[F, String] =
    for {
      client <- client.stream
      plainRequest <- Stream.eval[F, Request[F]](Applicative[F].pure[Request[F]](request))
      entityBody <- client.stream(plainRequest).flatMap(_.body.chunks).through(utf8DecodeC)
        .handleErrorWith { e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.eval(logs.log(Chain.one(s"${ex}")))
            .flatMap(a => Stream.emit(ex))
        }
    } yield entityBody

  def toApiResponseS: String => Stream[F, ApiResp] =
    in => {
      val json: Json = parse(in).getOrElse(Json.fromString(in))

      val _error = root.error.string

      val error: Option[String] = _error.getOption(json)

      Stream.emit(error match {
        case Some(a) => ApiError(json, a)
        case None => ApiOk(json)
      })
  }

  def toMaybeTypeS[A](implicit ev: Decoder[A]): String => Stream[F, Either[Chain[String], A]] =
    in => {
      val json: Json = parse(in).getOrElse(Json.fromString(in))

      val maybeA = json.as[A]

      Stream.emit(maybeA match {
        case Right(a) => Right(a)
        case Left(e) => Left(Chain.one(s"Error: ${e.printStackTraceAsString}: ${in}"))
      })
    }
  
  def readAllFromFile(path: String, bufferSize: Int = 8192): F[String] =
    for {
      str <- fs2.io.file.readAll(Paths.get(path), blocker, bufferSize)
        .through(utf8Decode)
        .handleErrorWith { e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.eval(logs.log(Chain.one(s"${ex}")))
            .flatMap(a => Stream.emit(ex))
        }.compile.toList
    } yield str.mkString("")

  def readByLinesFromFile(path: String, bufferSize: Int = 8192): Stream[F, String] =
    for {
      str <- fs2.io.file.readAll(Paths.get(path), blocker, bufferSize)
        .through(utf8Decode)
        .through(lines)
        .handleErrorWith { e =>
          val ex = WrapThrowable(e).printStackTraceAsString
          Stream.eval(logs.log(Chain.one(s"${ex}")))
            .flatMap(a => Stream.emit(ex))
        }
    } yield str

  private def createRequest(formData: UrlForm, inHeaders: Headers = Headers.empty) =
    Request[F](
      method = Method.POST,
      uri = Uri.unsafeFromString(conf.url),
      headers = headers ++ inHeaders ++ (inHeaders.exists(_.name == "Content-Type") match {
        case true => Headers.empty
        case _ => Headers.of(Header("Content-Type", "application/x-www-form-urlencoded"))
      }),
      httpVersion = HttpVersion.`HTTP/1.1`
    ).withEntity(formData)(UrlForm.entityEncoder(Charset.`UTF-8`))
    
  def createGetRequest(url: String, inHeaders: Headers = Headers.empty) =
    Request[F](
      method = Method.GET,
      uri = Uri.unsafeFromString(url),
      headers = headers ++ inHeaders ++ (inHeaders.exists(_.name == "Content-Type") match {
        case true => Headers.empty
        case _ => Headers.of(Header("Content-Type", "application/json"))
      }),
      httpVersion = HttpVersion.`HTTP/1.1`
    )
    
  override def importData[A](data: A, options: Chain[(String, String)], headers: Headers = Headers.empty)(implicit ev: Encoder[A]): Stream[F, ApiResp] = {

    val j: Json = data.asJson

    val formData = UrlForm.fromChain(defaultRequestBody <+> options) + ("data" -> j.noSpaces)

    // val a: Entity[F] = UrlForm.entityEncoder(charset).toEntity(formData)

    val request: Request[F] = createRequest(formData, headers)

    clientBodyStream(request)
      .flatMap(toApiResponseS)
  }

  override def exportData[A](options: Chain[(String, String)])(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]] = {
    // val m = mergeOptions(defaultRequestBody, options)
    // val formData = UrlForm.fromChain(m)

    val formData = UrlForm.fromChain(defaultRequestBody <+> options)

    val request: Request[F] = createRequest(formData)

    val fa = clientBodyStream(request)
      .compile.toVector.flatMap(_.mkString("").pure[F])

    Stream.eval(fa)
      .flatMap(toMaybeTypeS)
  }
  
  override def exportData(url: String, headers: Headers = Headers.empty): Stream[F, Json] = {
    val request: Request[F] = createGetRequest(url, headers)
    
    val fa = clientBodyStream(request)
      .compile.toVector.flatMap(_.mkString("").pure[F])
      
    Stream.eval(fa)  
      .flatMap(in => Stream.emit(parse(in).getOrElse(Json.fromString(in))))
  }
  
  override def exportDataT[A](url: String, headers: Headers = Headers.empty)(implicit ev: Decoder[A]): Stream[F, Either[Chain[String], A]] = {
    val request: Request[F] = createGetRequest(url, headers)
    
    val fa = clientBodyStream(request)
      .compile.toVector.flatMap(_.mkString("").pure[F])
    
    Stream.eval(fa)
      .flatMap(toMaybeTypeS)  
  }

  override def showLog: F[String] =
    for {
      l <- logs.get
    } yield l.show

  override def showConf: F[HttpConfig] =
    Sync[F].delay(conf)

}

object HttpInterpreter {
  def apply[F[_]: Functor: ConcurrentEffect: ContextShift[?[_]]: MonadLog[?[_], Chain[String]]](conf: HttpConfig): HttpInterpreter[F] =
    new HttpInterpreter[F](conf)
}
