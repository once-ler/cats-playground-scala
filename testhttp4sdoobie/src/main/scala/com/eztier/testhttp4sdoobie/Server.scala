package com.eztier.testhttp4sdoobie

import cats.data.{Chain, Writer}
import cats.effect._
import cats.syntax.all._
// import io.circe.config.parser
import io.circe.config.{parser => ConfigParser}

import org.http4s.server.{Router, Server => H4Server}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import doobie.util.ExecutionContexts
import doobie.hikari._
// App
import domain.authors._
import domain.api._
import infrastructure.endpoint.AuthorEndpoints
import infrastructure.repository.doobie.DoobieAuthorRepositoryInterpreter
import config.{DatabaseConfig, TestHttp4sDoobieConfig, HttpConfig}

import infrastructure.http._

import com.eztier.common.{MonadLog, _}

object Server extends IOApp {

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      implicit0(logs: MonadLog[F, Chain[String]]) <- Resource.liftF(MonadLog.createMonadLog[F, String])
      conf <- Resource.liftF(ConfigParser.decodePathF[F, TestHttp4sDoobieConfig]("testhttp4sdoobie"))
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      authorRepo = DoobieAuthorRepositoryInterpreter[F](xa)
      authorValidation = AuthorValidationInterpreter[F](authorRepo)
      authorService = AuthorService[F](authorRepo, authorValidation)
      
      apiRepoHttpConfig <- Resource.liftF(ConfigParser.decodePathF[F, HttpConfig](s"testhttp4sdoobie.http.local")) 
      apiRepo = HttpInterpreter[F](apiRepoHttpConfig)
      apiService = ApiService(apiRepo)
      
      httpApp = Router(
        "/authors" -> AuthorEndpoints.authorRoutes[F](authorService),
        "/api" -> AuthorEndpoints.apiRoutes[F](apiService)
      ).orNotFound
      _ <- Resource.liftF(DatabaseConfig.initializeDb(conf.db))
      server <- BlazeServerBuilder[F]
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] =
    createServer.use(_ => IO.never).as(ExitCode.Success)

  /*
  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(name) =>
        IO(println(s"Hello, $name.")).as(ExitCode.Success)
      case None =>
        IO(System.err.println("Usage: MyApp name")).as(ExitCode(2))
    }
  */
}
