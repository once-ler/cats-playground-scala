package com.eztier.testhttp4sdoobie.domain
package authors

import cats.Functor
import cats.data.EitherT

/**
  * The entry point to our domain, works with repositories and validations to implement behavior
  * @param authorRepo where we get our data
  * @param validation something that provides validations to the service
  * @tparam F - this is the container for the things we work with, could be scala.concurrent.Future, Option, anything
  *           as long as it is a Monad
  */
class AuthorService[F[_]](authorRepo: AuthorRepositoryAlgebra[F], validation: AuthorValidationAlgebra[F]) {
  def getAuthor(id: Long)(implicit F: Functor[F]): EitherT[F, AuthorNotFoundError.type, Author] =
    authorRepo.get(id).toRight(AuthorNotFoundError)

  def getAuthorByEmail(email: String)(implicit F: Functor[F]): EitherT[F, AuthorNotFoundError.type, Author] =
    authorRepo.findByEmail(email).toRight(AuthorNotFoundError)
}

object AuthorService {
  def apply[F[_]](repository: AuthorRepositoryAlgebra[F], validation: AuthorValidationAlgebra[F]): AuthorService[F] =
    new AuthorService[F](repository, validation)
}
