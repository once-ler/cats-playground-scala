package com.eztier.testhttp4sdoobie.domain

import authors.Author

sealed trait ValidationError extends Product with Serializable

case object PublicationNotFoundError extends ValidationError
case object AuthorNotFoundError extends ValidationError
case class AuthorAlreadyExistsError(author: Author) extends ValidationError
