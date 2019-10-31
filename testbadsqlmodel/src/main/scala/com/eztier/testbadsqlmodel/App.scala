package com.eztier.testbadsqlmodel

import cats.implicits._
import cats.effect._
import cats.effect.{Async, ContextShift, IOApp, Resource}
import com.eztier.testbadsqlmodel.domain.trials.{JunctionService, TrialAggregator, TrialArmService, TrialArmValidationInterpreter, TrialContractService, TrialContractValidationInterpreter, TrialService, TrialValidationInterpreter}
import io.circe.config.{parser => ConfigParser}
import doobie.util.ExecutionContexts
import config._
import infrastructure.repository.doobie.{DoobieJunctionRepositoryInterpreter, DoobieTrialArmRepositoryInterpreter, DoobieTrialContractRepositoryInterpreter, DoobieTrialRepositoryInterpreter}

object App extends IOApp {

  // ConfigParser.decodePathF[F, DatabaseConfig]("testbadsqlmodel") // Decode an instance supporting [[cats.ApplicativeError]]
  def createTrialAggregator[F[_]: Async: ContextShift: ConcurrentEffect] =
    for {
      conf <- Resource.liftF(ConfigParser.decodePathF[F, AppConfig]("testbadsqlmodel")) // Lifts an applicative into a resource.
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      trialRepo = DoobieTrialRepositoryInterpreter[F](xa)
      trialValidation = TrialValidationInterpreter[F](trialRepo)
      trialService = TrialService(trialRepo, trialValidation)
      trialArmRepo = DoobieTrialArmRepositoryInterpreter[F](xa)
      trialArmValidation = TrialArmValidationInterpreter[F](trialArmRepo)
      trialArmService = TrialArmService(trialArmRepo, trialArmValidation)
      trialContractRepo = DoobieTrialContractRepositoryInterpreter[F](xa)
      trialContractValidation = TrialContractValidationInterpreter[F](trialContractRepo)
      trialContractService = TrialContractService(trialContractRepo, trialContractValidation)
      junctionRepo = DoobieJunctionRepositoryInterpreter[F](xa)
      junctionService = JunctionService(junctionRepo)
      trialAggregator = TrialAggregator(trialService, trialArmService, trialContractService, junctionService)
    } yield trialAggregator


  // val agg = createTrialAggregator.use(_.run.compile.drain)

  override def run(args: List[String]): IO[ExitCode] = IO(println("")).as(ExitCode.Success)

}
