package com.eztier.testbadsqlmodel
package domain.trials

// import java.util.concurrent.Executors
// import scala.concurrent.ExecutionContext

import cats.{Applicative, Show}
import cats.effect.IO
import cats.implicits._
import cats.effect._
import fs2.{Stream}

class TrialAggregator[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) {
  // implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  // implicit val concurrent = IO.ioConcurrentEffect(IO.contextShift(ec))

  // For showLinesStdOut
  implicit val showTrialArm: Show[TrialArm] = Show.show(t => s"${t.id}\n${t.name}")

  // EitherT[F, A, B] == F[Either[A, B]]
  def run  = {
    // https://medium.com/@scalaisfun/optiont-and-eithert-in-scala-90241aba1bb7

    val L = implicitly[LiftIO[F]]

    val action = for {
      a <- trialContractService.get(1001)
      b <- trialService.exists(a.trialId)
      d <- junctionService.list(b.trialArmSet)
    } yield d

    val mi = action.fold(
      a =>
        List[Junction](),
      b =>
        b
    )

    val maybeItems = action.value.map {
      case Right(list) => list
      case Left(_) => List[Junction]()
    }

    val concurrency = 4

    // def eval[F[_],A](f: F[A]): Stream[F,A]
    val list = Stream.eval(maybeItems)
      .flatMap(Stream.emits)
      // .chunkN(concurrency)
      .parEvalMapUnordered(concurrency)(a => trialArmService.get(a.itemId).value)
      // .through(filterLeft)
      // .covary[F]
      .showLinesStdOut
      // .evalTap(a  => IO {println(s"${a.id} ${a.name}")})
      // .compile
      // .toList

    list
  }
}

object TrialAggregator {
  def apply[F[_]: Applicative: Async: Concurrent](trialService: TrialService[F], trialArmService: TrialArmService[F], trialContractService: TrialContractService[F], junctionService: JunctionService[F]) =
    new TrialAggregator[F](trialService, trialArmService, trialContractService, junctionService)
}
