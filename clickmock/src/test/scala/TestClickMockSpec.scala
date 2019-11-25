package com.eztier.clickmock
package test

import org.specs2.mutable._
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.config.{parser => ConfigParser}
import cats.effect._
import com.eztier.clickmock.infrastructure.doobie.{DoobieCkPartyRepositoryInterpreter, DoobieCkPersonRepositoryInterpreter, DoobieCkResourceRepositoryInterpreter, DoobieCk_ParticipantCustomExtensionRepositoryInterpreter, DoobieCk_ParticipantRepositoryInterpreter, DoobieCk_PersonCustomExtensionRepositoryInterpreter}
import com.eztier.clickmock.infrastructure.soap.{CkEntityInterpreter, CkMockService}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import fs2.Stream
import doobie.util.ExecutionContexts
import domain._
import infrastructure.soap._
import infrastructure.soap.entity._
import config._
import doobie.util.transactor.Transactor

object FauxWeb {
  case class SomeXmlResponse(xml: String)

  implicit def oneXmlEncoder[F[_]]: EntityEncoder[F, SomeXmlResponse] = ???
  implicit def manyXmlEncoder[F[_]]: EntityEncoder[F, Seq[SomeXmlResponse]] = ???

  trait RpcRepo[F[_]] {
    def process(request: String): F[Option[SomeXmlResponse]]
  }

  def rpcService[F[_]](repo: Option[RpcRepo[F]] = None)(implicit F: Effect[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "soap" => {

      req.decode[String] { m =>

        val action = for {
          c <- req.headers.get(`Content-Type`)
          a <- c.mediaType.extensions.get("action")
        } yield a

        val body = action.fold("") {
          sa =>
            val soapAction = sa.split('/').last

            soapAction match {
              case "Login" =>
                val obj = LoginResponse(Some("abc123"))
                val loginResponse = scalaxb.toXML[LoginResponse](obj, "LoginResponse", com.eztier.clickmock.infrastructure.soap.entity.defaultScope)
                loginResponse.toString()
              case "getEntityByID" =>
                val obj = GetEntityByIDResponse(Some("<mainspan />"))
                val getEntityByIDResponse = scalaxb.toXML[GetEntityByIDResponse](obj, "getEntityByIDResponse", entity.defaultScope)
                getEntityByIDResponse.toString()
              case "redefineEntityByID" =>
                val obj = RedefineEntityByIDResponse(Some("<mainspan />"))
                val redefineEntityByIDResponse = scalaxb.toXML[RedefineEntityByIDResponse](obj, "redefineEntityByIDResponse", entity.defaultScope)
                redefineEntityByIDResponse.toString()
              case "createEntity" =>
                val obj = CreateEntityResponse(Some("<mainspan />"))
                val createEntityResponse = scalaxb.toXML[CreateEntityResponse](obj, "createEntityResponse", entity.defaultScope)
                createEntityResponse.toString()
              case "Logoff" =>
                val obj = LogoffResponse()
                val logoffResponse = scalaxb.toXML[LogoffResponse](obj, "LogoffResponse", entity.defaultScope)
                logoffResponse.toString()
              case _ =>
                import soapenvelope12._

                val obj = soapenvelope12.Fault(
                  Faultcode(FaultcodeEnum.fromString("BS", entity.defaultScope)),
                  Faultreason(Reasontext("Badness", "en-US"))
                )

                scalaxb.toXML[Fault](obj, "Fault", entity.defaultScope).toString()
            }
        }

        val xml = s"""<?xml version="1.0"?><soap:Envelope
xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
soap:encodingStyle="http://www.w3.org/2003/05/soap-encoding"><soap:Body>$body</soap:Body></soap:Envelope>"""

        F.pure(Response(status = Status.Ok)
          .withContentType(`Content-Type`(MediaType.text.xml))
          .withEntity(xml)
        )

      }

    }
  }

  def helloWorldService[F[_]: Effect]: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" / name =>
      Effect[F].pure(Response(Status.Ok).withEntity(s"Hello, $name."))
  }

  def createHttpServer[F[_]: Effect : ConcurrentEffect : Timer] = {
    val httpApp = Router("/" -> helloWorldService[F], "/api" -> rpcService[F]()).orNotFound

    val serverBuilder = BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)

    serverBuilder

    // Run in background
    // val fiber = serverBuilder.resource.use(_ => IO.never).start.unsafeRunSync()
  }

}

class TestClickMockSpec[F[_]] extends Specification {
  val ec = scala.concurrent.ExecutionContext.global
  implicit val timer = IO.timer(ec)
  implicit val cs = IO.contextShift(ec)  // Need cats.effect.ContextShift[cats.effect.IO] because not inside of IOApp

  def blockingThreadPool[F[_]](implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newCachedThreadPool()
      val ec = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })

  def createMockService[F[_]: Async: ContextShift: ConcurrentEffect: Timer]: Resource[F, CkMockService[F]] = {

    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("clickmock")) // Lifts an applicative into a resource.
      b = blockingThreadPool[F]
      ms = CkMockService(conf, b)
    } yield ms
  }

  def createDoobieService[F[_]: Async :ContextShift :ConcurrentEffect: Timer] = {
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("clickmock"))
      _ <- Resource.liftF(DatabaseConfig.initializeDb[F](conf.db.local)) // Lifts an applicative into a resource. Resource[Tuple1, Nothing[Unit]]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.local.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor[F](conf.db.local, connEc, Blocker.liftExecutionContext(txnEc))
      b = blockingThreadPool[F]
      ms = CkMockService(conf, b)
      entityRepo = CkEntityInterpreter[F](ms)
      entityService = CkEntityService(entityRepo)
      participantRepo = DoobieCk_ParticipantRepositoryInterpreter[F](xa)
      participantService = Ck_ParticipantService(participantRepo)
      participantCustomExtensionRepo = DoobieCk_ParticipantCustomExtensionRepositoryInterpreter[F](xa)
      participantCustomExtensionService = Ck_ParticipantCustomExtensionService(participantCustomExtensionRepo)
      personRepo = DoobieCkPersonRepositoryInterpreter[F](xa)
      personService = CkPersonService(personRepo)
      personCustomExtensionRepo = DoobieCk_PersonCustomExtensionRepositoryInterpreter[F](xa)
      personCustomExtensionService = Ck_PersonCustomExtensionService(personCustomExtensionRepo)
      partyRepo = DoobieCkPartyRepositoryInterpreter[F](xa)
      partyService = CkPartyService(partyRepo)
      resourceRepo = DoobieCkResourceRepositoryInterpreter[F](xa)
      resourceService = CkResourceService(resourceRepo)
      participantAggregator = Ck_ParticipantAggregator(entityService, participantService, participantCustomExtensionService, personService, personCustomExtensionService, partyService, resourceService)
    } yield participantAggregator

  }

  // import doobie.util.ExecutionContexts
  // implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  def getTestRemoteDoobieTransactor = Transactor.fromDriverManager[IO](
    "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "jdbc:sqlserver://localhost:1433;DatabaseName=test",
    "admin",
    "12345678",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def getTestLocalDoobieTransactor = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MSSQLServer",
    "sa",
    "",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  "CkMockService" should {
    "Initialize" in {

      // Make sure to change the VM parameters:
      // -Dclickmock.soap.url=http://localhost:8080/api/soap

      import FauxWeb._

      // Server
      val fiberThread = createHttpServer[IO].resource.use(_ => IO.never).start.unsafeRunSync()

      // Client
      val resources = createMockService[IO]

      resources.use{
        mock =>

          val a = Stream.eval(mock.tryGetEntityByID(Some("123"))).compile.toList
          val b = a.unsafeRunSync()

          IO(println(b))
      }.unsafeRunSync()

      1 mustEqual 1
    }

    "Doobie find" in {

      val doobieResource = createDoobieService[IO]
      val xa = getTestLocalDoobieTransactor

      doobieResource.use {
        z =>

          val doobieInterpreter = DoobieCk_ParticipantRepositoryInterpreter(xa)
          val s = doobieInterpreter.findById(Some("bogus_id")).value
          val r = s.unsafeRunSync()

          val doobieInterpreter2 = DoobieCkPartyRepositoryInterpreter(xa)
          val s2 = doobieInterpreter2.findByOid(Some("AB")).value
          val r2 = s2.unsafeRunSync()

          IO(println(r2))
      }.unsafeRunSync()

      1 mustEqual 1
    }

    "Doobie Aggregator" in {

      /*
      // akka graph dsl

      bcast ~>
      createPersonExtension ~>
        createPerson ~>
          getPartyContactInformation ~>
            createPartyContactInformation ~>
              createParty ~> zip.in0

      bcast ~>
        createParticipantCustomExtension ~> zip.in1

      zip.out ~>
        mergePersonAndParticipantExtension ~>
          createParticipant ~> merge

      merge.out ~>
        getParticipantRecords ~>
          createParticipantRecordContactInformation
      */

      def createPersonExtension[F[_]](mock: CkMockService[F]) = {

      }

      createDoobieService[IO].use {
        agg =>
          val a = agg.getParticipant(Some("10042419")).unsafeRunSync()


          IO(println("Done"))
      }.unsafeRunSync()

      1 mustEqual 1
    }

  }
}
