package com.eztier.testbadsqlmodel.config

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)

case class DatabaseConfig(
  url: String,
  driver: String,
  user: String,
  password: String,
  locations: Option[String] = None,
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
          .locations(cfg.locations.getOrElse(""))
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      }
      fw.migrate()
    }.as(())
}
