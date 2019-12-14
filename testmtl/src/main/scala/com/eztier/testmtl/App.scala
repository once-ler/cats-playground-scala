package com.eztier.testmtl

import cats.implicits._
import cats.mtl.implicits._
import cats.{Functor, Monad}
import cats.data.{Chain, EitherT, OptionT, ReaderWriterStateT, Writer}
import cats.implicits._
import cats.effect.{Async, Blocker, Bracket, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.mtl.FunctorTell
import com.eztier.testmtl.Pakage.{RW}
import doobie._
import doobie.hikari._
import doobie.implicits._
import doobie.util.ExecutionContexts
import fs2.Stream
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)

case class DatabaseConfig
(
  url: String,
  driver: String,
  user: String,
  password: String,
  connections: DatabaseConnectionsConfig
)

object DatabaseConfig {
  def dbTransactor[F[_] : Async : ContextShift]
  (
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

  case class VariableGeneralItem
  (
    id: Long = -1,
    quantity: Int = 0,
    sponsorCost: Double = 0.00
  )

  trait VariableGeneralItemRepositoryAlgebra[F[_]] {
    def get(id: Long): OptionT[F, VariableGeneralItem]

    def getF(id: Long): F[Option[VariableGeneralItem]]
  }

  class VariableGeneralItemService[F[_]](
    repository: VariableGeneralItemRepositoryAlgebra[F]
  ) {
    def get(id: Long)(implicit F: Functor[F]): EitherT[F, String, VariableGeneralItem] =
      repository.get(id).toRight("Variable procedure item not found.")

    def getWithLog[F[_]](id: Long)(implicit F: Monad[F], W: FunctorTell[F, Chain[String]]) = {
      for {
        _ <- W.tell(Chain.one(s"Processing ${id}"))
        result <- repository.getF(id)
      } yield result
    }
  }

  object VariableGeneralItemService {
    def apply[F[_]](
      repository: VariableGeneralItemRepositoryAlgebra[F]
    ): VariableGeneralItemService[F] =
      new VariableGeneralItemService[F](repository)
  }
}

object Infrastucture {
  import Domain._

  private object VariableGeneralItemSql {
    def get(id: Long): Query0[VariableGeneralItem] = sql"""
    SELECT id, quantity, sponsor_cost
    FROM variable_general_item
    WHERE id = $id
  """.query
  }

  class DoobieVariableGeneralItemRepositoryInterpreter[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
    extends VariableGeneralItemRepositoryAlgebra[F] {

    override def get(id: Long): OptionT[F, VariableGeneralItem] = OptionT(VariableGeneralItemSql.get(id).option.transact(xa))

    override def getF(id: Long): F[Option[VariableGeneralItem]] = VariableGeneralItemSql.get(id).option.transact(xa)
  }

  object DoobieVariableGeneralItemRepositoryInterpreter {
    def apply[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieVariableGeneralItemRepositoryInterpreter[F] =
      new DoobieVariableGeneralItemRepositoryInterpreter(xa)
  }
}

object Pakage {
  // https://medium.com/@alexander.zaidel/readerwriterstate-monad-in-action-98c3a4561df3
  // https://medium.com/@alexander.zaidel/the-beauty-of-final-tagless-and-cats-mtl-68abdc8d720
  /**
    * Represents a stateful computation in a context `F[_]`, over state `S`, with an
    * initial environment `E`, an accumulated log `L` and a result `A`.
    */
  // type ReaderWriterStateT[F[_], E, L, S, A] = IndexedReaderWriterStateT[F, E, L, S, S, A]

  type RW[T] = ReaderWriterStateT[IO, Int, Chain[String], Map[String, String], T]
}

object App extends IOApp {
  val conf = DatabaseConfig(url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "sa", password = "",  connections = DatabaseConnectionsConfig(10))

  import Package._
  import Domain._
  import Infrastucture._

  def getResource[F[_]: ContextShift: Async] = {
    for {
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf)) // Lifts an applicative into a resource. Resource[Tuple1, Nothing[Unit]]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf, connEc, Blocker.liftExecutionContext(txnEc)) // Creates a blocker that delegates to the supplied execution context.
      varianbleCostRepo = DoobieVariableGeneralItemRepositoryInterpreter(xa)
      variableCostService = VariableGeneralItemService(varianbleCostRepo)
    } yield variableCostService
  }

  getResource[IO].use {
    a =>

      val result = a.getWithLog[RW](1).run(2, Map("user" -> "foo"))

      IO.unit
  }

  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}