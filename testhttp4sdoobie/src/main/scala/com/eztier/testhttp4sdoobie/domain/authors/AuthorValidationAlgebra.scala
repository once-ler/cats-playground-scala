package com.eztier.testhttp4sdoobie.domain
package authors

import cats.data.EitherT

trait AuthorValidationAlgebra[F[_]] {
  /* Fails with a AuthorAlreadyExistsError */
  def doesNotExist(author: Author): EitherT[F, AuthorAlreadyExistsError, Unit]

  /* Fails with a AuthorNotFoundError if the author id does not exist or if it is none */
  def exists(authorId: Option[Long]): EitherT[F, AuthorNotFoundError.type, Unit]
}
