package src.com.eztier.testbadsqlmodel

import cats.effect.{Async, ContextShift, IOApp}
import io.circe.config.{parser => ConfigParser}

object App extends IOApp {

  def setupDatabase[F[_]: Async: ContextShift] = {

  }

}
