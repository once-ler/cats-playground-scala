package com.eztier.testbadsqlmodel
package domain.trials

import cats.data.EitherT
import cats.{Applicative, Show}
import cats.implicits._
import cats.effect._
import fs2.{Pipe, Stream}

class TrialAggregator[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], variableProcedureItemService: VariableProcedureItemService[F], variableGeneralItemService: VariableGeneralItemService[F], junctionService: JunctionService[F]) {
  // For showLinesStdOut
  implicit val showTrialArm: Show[TrialArm] = Show.show(t => s"${t.id}\n${t.name}")
  implicit val showTuple: Show[(TrialContract, Trial, List[TrialArm])] =
    Show.show(t => s"Contract id: ${t._1.id}\nContract name: ${t._1.name}\nTrial id: ${t._2.id}\nTrial name: ${t._2.name}\nArms count: ${t._3.length}")

  val concurrency = 4

  def getJunctions(id: Option[Long]): F[List[Junction]] = {
    junctionService.list(id)
      .fold(
        err => {
          println(err) // TODO: logger
          List[Junction]()
        },
        li => li
      )
  }

  def parGetJunctionItems[A](junctions: F[List[Junction]], fa: Junction => F[Either[String, A]]): F[List[A]] = {
    Stream.eval(junctions)
      .flatMap(Stream.emits)
      .parEvalMapUnordered(concurrency)(fa)
      .through(filterLeft)
      .compile.toList
  }

  def parGetArmsOrig: Pipe[F, (TrialContract, Trial, List[Junction]), (TrialContract, Trial, List[TrialArm])] =
    _.evalMap {
      in =>
        val f3 = Applicative[F].pure(in._3)

        val arms =
          Stream
            .eval(f3)
            .flatMap(Stream.emits)
            .parEvalMapUnordered(concurrency)(a => trialArmService.get(a.itemId).value)
            .through(filterLeft)
            .compile.toList

        arms.map {
          l =>
            (in._1, in._2, l)
        }
    }

  def parGetArms: Pipe[F, (TrialContract, Trial), (TrialContract, Trial, List[TrialArm])] =
    _.evalMap {
      in =>
        val armJunctions = getJunctions(in._2.trialArmSet)
        val arms = parGetJunctionItems[TrialArm](armJunctions, b => trialArmService.get(b.itemId).value)

        arms.map {
          l =>
            (in._1, in._2, l)
        }
    }

  def parGetVariableCosts: Pipe[F, (TrialContract, Trial, List[TrialArm]), Unit] =
    _.evalMap {
      in =>
        val A = Applicative[F]
        val f3 = A.pure(in._3)

        val varProcedureCosts = Stream.eval(f3)
          .flatMap(Stream.emits)
          .parEvalMapUnordered(concurrency) {
            a =>

              val procedureJunctions = getJunctions(a.variableProcedureItemSet)

              val generalJunctions = getJunctions(a.variableGeneralItemSet)

              val procedureItems = parGetJunctionItems[VariableProcedureItem](procedureJunctions, b => variableProcedureItemService.get(b.itemId).value)

              val generalItems = parGetJunctionItems[VariableGeneralItem](generalJunctions, b => variableGeneralItemService.get(b.itemId).value)

              /*
              val procedureItems = Stream.eval(procedureJunctions)
                .flatMap(Stream.emits)
                .parEvalMapUnordered(concurrency) {
                  b => variableProcedureItemService.get(b.itemId).value
                }
                .through(filterLeft)
                .compile.toList
              */

              ???
          }

        /*
        .parEvalMapUnordered(concurrency)(a => junctionService.list(a.variableProcedureItemSet).value)
        .through(filterLeft)
        .flatMap(Stream.emits)
        .parEvalMapUnordered(concurrency)(a => variableProcedureItemService.get(a.itemId).value)
        .compile.toList
        */
        ???
    }

  def runByTrialContract[F](id: Long) = {
    val action = (for {
      a <- trialContractService.get(id)
      b <- trialService.exists(a.trialId)
    } yield (a, b))
      .fold(
        err => {
          println(err) // TODO: logger

          (TrialContract(), Trial())
        },
        resp => resp
      )

    action
  }

  def runByTrial[F](id: Long) = {
    val action = (for {
      a <- trialService.get(id)
      b <- trialContractService.getByTrialAndSponsor(Some(id), a.sponsor)
    } yield (b, a))
      .fold(
        err => {
          println(err) // TODO: logger

          (TrialContract(), Trial())
        },
        resp => resp
      )

    action
  }

  // EitherT[F, A, B] == F[Either[A, B]]
  def run(id: Long)  = {
    // https://medium.com/@scalaisfun/optiont-and-eithert-in-scala-90241aba1bb7
    /*
    val action = (for {
      a <- trialContractService.get(id)
      b <- trialService.exists(a.trialId)
      d <- junctionService.list(b.trialArmSet)
    } yield (a, b, d))
      .fold(
        err => {
          println(err) // TODO: logger

          (TrialContract(), Trial(), List[Junction]())
        },
        resp => resp
      )

    // def eval[F[_],A](f: F[A]): Stream[F,A]
    Stream.eval(action)
      .through(parGetArms)
      .showLinesStdOut

     */

    Stream.eval(runByTrial(1002))
      .through(parGetArms)
      .showLinesStdOut

  }
}

object TrialAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], variableProcedureItemService: VariableProcedureItemService[F], variableGeneralItemService: VariableGeneralItemService[F], junctionService: JunctionService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService, variableProcedureItemService, variableGeneralItemService, junctionService)
}
