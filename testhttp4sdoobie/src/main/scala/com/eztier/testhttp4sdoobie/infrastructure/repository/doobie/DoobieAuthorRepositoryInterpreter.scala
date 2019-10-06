package com.eztier.testhttp4sdoobie
package infrastructure.repository.doobie

import cats.Monad
import doobie._
import doobie.implicits._
import cats.data.OptionT
import cats.effect.{Bracket, IO}
import cats.data._
import cats.implicits._
import domain.authors.{Author, AuthorRepositoryAlgebra}

private object AuthorSQL {

  def findOneSql(id: Long): Query0[Author] = sql"""
    SELECT first_name, last_name, email, phone, id
    FROM author
    WHERE id = $id
  """.query

  def findByEmailSql(email: String): Query0[Author] = sql"""
    SELECT first_name, last_name, email, phone, id
    FROM author
    WHERE email = $email
  """.query
}

// Kind projector
// EitherT[*[_], Int, *]    // equivalent to: type R[F[_], B] = EitherT[F, Int, B]
// Bracket extends MonadError.
// transact requires implicit ev: Bracket[M, Throwable]
// Using kind projector will produce bracket[?[_], Throwable] at compile time.
class DoobieAuthorRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends AuthorRepositoryAlgebra[F] { self =>
  import AuthorSQL._

  def get(id: Long): OptionT[F, Author] = OptionT(findOneSql(id).option.transact(xa))

  def findByEmail(email: String): OptionT[F, Author] =
    OptionT(findByEmailSql(email).option.transact(xa))
}

object DoobieAuthorRepositoryInterpreter {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieAuthorRepositoryInterpreter[F] =
    new DoobieAuthorRepositoryInterpreter(xa)
}
