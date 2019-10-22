package com.eztier.testxmlfs2

import cats.implicits._
import cats.effect.{Async, Blocker, Bracket, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import fs2.Stream
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)

case class DatabaseConfig(
  url: String,
  driver: String,
  user: String,
  password: String,
  connections: DatabaseConnectionsConfig
)

object DatabaseConfig {
  def dbTransactor[F[_] : Async : ContextShift](
    dbc: DatabaseConfig,
    connEc: ExecutionContext,
    blocker: Blocker,
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)

  // By default, flyway will look at ./my-project/src/main/resources/db/migration for versioned sql files.
  def initializeDb[F[_]](cfg: DatabaseConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway = {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      }
      fw.migrate()
    }.as(())
}

object Domain {
  case class Place(
    code: String,
    display: String
  )
}

class Miner[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F]) {
  import Domain._

  def oneSql(p: String): Query0[Place] = sql"""
    SELECT code, display
    FROM place where code = '$p'
  """.query

  def getOnePlace(p: String): Stream[F, Place] = {
    oneSql(p).stream.transact(xa)
  }

}

object Miner {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): Miner[F] = new Miner(xa)
}
