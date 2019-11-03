package com.eztier.testbadsqlmodel
package domain.trials

import cats.{Applicative, Show}
import cats.implicits._
import cats.effect._
import fs2.{Pipe, Stream}

class TrialAggregator[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) {
  // For showLinesStdOut
  implicit val showTrialArm: Show[TrialArm] = Show.show(t => s"${t.id}\n${t.name}")
  implicit val showTuple: Show[(TrialContract, Trial, List[TrialArm])] =
    Show.show(t => s"Contract id: ${t._1.id}\nContract name: ${t._1.name}\nTrial id: ${t._2.id}\nTrial name: ${t._2.name}\nArms count: ${t._3.length}")

  // EitherT[F, A, B] == F[Either[A, B]]
  def run(id: Long)  = {
    // https://medium.com/@scalaisfun/optiont-and-eithert-in-scala-90241aba1bb7

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

    val concurrency = 4

    def parGetArms: Pipe[F, (TrialContract, Trial, List[Junction]), (TrialContract, Trial, List[TrialArm])] =
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

    def parGetVariableCosts: Pipe[F, (TrialContract, Trial, List[TrialArm]), ()] =
      _.evalMap {
        in =>
          val f3 = Applicative[F].pure(in._3)

          val varProcedureCosts = Stream.eval(f3)
            .flatMap(Stream.emits)
            .parEvalMapUnordered(concurrency)(a => junctionService.list(a.variableProcedureItemSet).value)
            .through(filterLeft)
            .flatMap(Stream.emits)
            .parEvalMapUnordered(concurrency)(a => ???)
            .compile.toList

          ???
      }

    // def eval[F[_],A](f: F[A]): Stream[F,A]
    Stream.eval(action)
      .through(parGetArms)
      .showLinesStdOut
  }
}

object TrialAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService, junctionService)
}
