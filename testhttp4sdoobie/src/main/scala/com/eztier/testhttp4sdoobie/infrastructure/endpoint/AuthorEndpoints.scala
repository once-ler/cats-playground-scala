package com.eztier.testhttp4sdoobie
package infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import com.eztier.testhttp4sdoobie.domain.AuthorNotFoundError
import com.eztier.testhttp4sdoobie.domain.authors.AuthorService
import org.http4s.circe._
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import cats.syntax.semigroupk._

class AuthorEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  private def getAuthorEndpoint(authorService: AuthorService[F]) : PartialFunction[Request[F], F[Response[F]]] = {
    case GET -> Root / LongVar(id) =>
      authorService.getAuthor(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(AuthorNotFoundError) => NotFound("The author was not found")
      }
  }

  private def getAuthorListEndpoint(authorService: AuthorService[F]) : PartialFunction[Request[F], F[Response[F]]] = {
    case GET -> Root / "all" =>
      Ok(authorService.listAuthors.map(_.asJson))
  }

  def endpoints(
    authorService: AuthorService[F]
  ): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      getAuthorEndpoint(authorService)
        .orElse(getAuthorListEndpoint(authorService))
    }
  }

}

object AuthorEndpoints {
  def endpoints[F[_]: Sync](
    authorService: AuthorService[F]
  ): HttpRoutes[F] =
    new AuthorEndpoints[F].endpoints(authorService)
}
