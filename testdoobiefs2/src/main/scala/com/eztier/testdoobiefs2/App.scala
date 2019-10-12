package com.eztier.testdoobiefs2

import cats.implicits._
import cats.effect.{Async, Blocker, Bracket, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import doobie._
import doobie.hikari._
import doobie.implicits._
import doobie.util.ExecutionContexts
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

  def transform1(a: Stream[F, Author]): Stream[F, (String, String)] = {
    a.flatMap(b => Stream.emit(b.firstName, b.lastName))
  }

  def transform2(a: Stream[F, (String, String)]) = {
    a.map(b => b._2.asRight[Int])
  }
}

object Miner {
  def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): Miner[F] = new Miner(xa)
}

object App extends IOApp {
  val conf = DatabaseConfig(url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "sa", password = "",  connections = DatabaseConnectionsConfig(10))

  def getMiner[F[_]: Async: ContextShift] = {
    for {
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf)) // Lifts an applicative into a resource. Resource[Tuple1, Nothing[Unit]]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf, connEc, Blocker.liftExecutionContext(txnEc)) // Creates a blocker that delegates to the supplied execution context.
      miner = Miner(xa)
    } yield miner
  }

  val miner = getMiner[IO]

  val stream1 = miner.use(a => a.getAuthors
    .through(a.transform1)
    .through(a.transform2)
    .compile.drain)

  override def run(args: List[String]): IO[ExitCode] = stream1.as(ExitCode.Success)
}
