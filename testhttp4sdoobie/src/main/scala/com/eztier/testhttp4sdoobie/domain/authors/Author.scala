package com.eztier.testhttp4sdoobie.domain
package authors

case class Author(
  firstName: String,
  lastName: String,
  email: String,
  phone: String,
  id: Option[Long] = None
)
