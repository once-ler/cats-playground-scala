package com.eztier.testdoobiefs2

import cats.effect.{Bracket, ExitCode, IO, IOApp}
import doobie._
import doobie.implicits._
import fs2.Stream

object Domain {
  case class Author(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    id: Option[Long] = None
  )
}

class Miner[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F]) {
  import Domain._

  def listSql: Query0[Author] = sql"""
    SELECT first_name, last_name, email, phone, id
    FROM author
  """.query

  def getAuthors: Stream[F, Author] = {
    listSql.stream.transact(xa)
  }
}

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = ???
}
