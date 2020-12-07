package com.eztier.testhttp4sdoobie
package infrastructure.endpoint

import fs2.Stream
import fs2.concurrent.Queue
import cats.effect.{Sync, Concurrent, ContextShift}
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Json, Decoder, Encoder}

import com.eztier.testhttp4sdoobie.domain.AuthorNotFoundError
import com.eztier.testhttp4sdoobie.domain.authors.AuthorService
import com.eztier.testhttp4sdoobie.domain.api.ApiService
import com.eztier.testhttp4sdoobie.domain.users.{User, Post}

import org.http4s._
import org.http4s.circe._
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

class AuthorEndpoints[F[_]: Sync : ContextShift : Concurrent] extends Http4sDsl[F] {

  // implicit def jsonDecoder[A : Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  private def getAuthorEndpoint(authorService: AuthorService[F]) : PartialFunction[Request[F], F[Response[F]]] = {
    case GET -> Root / LongVar(id) =>
      authorService.getAuthor(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(AuthorNotFoundError) => NotFound("The author was not found")
      }
  }
  
  private def mergeMultipleResponses(apiService: ApiService[F]): PartialFunction[Request[F], F[Response[F]]] = {
    case GET -> Root / LongVar(id) =>
      
      val url0 = "https://jsonplaceholder.typicode.com/users"
      val url1 = "https://jsonplaceholder.typicode.com/posts"
      
      // Ok(apiService.exportData(url0))
      
      /*
      Ok(
      Stream.eval(apiService.exportDataT[List[User]](url0)
        .compile.toVector).flatMap { v =>
          v.head match {
            case Right(found) =>
              Stream.emits(found.asJson)              
            case Left(error) =>
              Stream.emit(error.show.asJson)
          }
        }
      )
      */
      
      Ok(
      Stream.emits(List(url0, url1))
        .covary[F]
        .parEvalMap(2)(u => apiService.exportData(u).compile.toVector)
        .compile.toVector.flatMap { res =>
          val l = res(0)
          val l0 = l(0)
          val a = l0.as[List[User]]
          
          val users = a match {
            case Right(d) => d
            case Left(e) => List.empty[User]
          }
          
          val la = res(1)
          val l1 = la(0)
          val b = l1.as[List[Post]]
          
          val posts = b match {
            case Right(d) => d
            case Left(e) => List.empty[Post]
          }
          
          posts.asJson.pure[F]
        }
      )
      
  }
  
  def authorRoutes(authorService: AuthorService[F]) =
    HttpRoutes.of[F] {
      getAuthorEndpoint(authorService)
    }
    
  def apiRoutes(apiService: ApiService[F]) =
    HttpRoutes.of[F] {
      mergeMultipleResponses(apiService)
    }
}

object AuthorEndpoints {
  def authorRoutes[F[_]: Sync : ContextShift : Concurrent](
    authorService: AuthorService[F]
  ): HttpRoutes[F] =
    new AuthorEndpoints[F].authorRoutes(authorService)
    
  def apiRoutes[F[_]: Sync : ContextShift : Concurrent](
    apiService: ApiService[F]
  ): HttpRoutes[F] =
    new AuthorEndpoints[F].apiRoutes(apiService)  
}
