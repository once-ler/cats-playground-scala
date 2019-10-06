package com.eztier.testhttp4sdoobie.domain
package authors

import cats.data.OptionT

trait AuthorRepositoryAlgebra[F[_]] {
  // Handles the outer map.
  // val customGreetingT: OptionT[Future, String] = OptionT(customGreeting)
  // remove the boilerplate of:
  // val excitedGreeting: Future[Option[String]] = customGreeting.map(_.map(_ + "!"))
  def get(id: Long): OptionT[F, Author]

  def findByEmail(email: String): OptionT[F, Author]
}
