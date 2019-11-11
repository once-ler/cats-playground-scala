package com.eztier.testxmlfs2

import fs2.{Pipe, Stream}
import cats.syntax.option._ // for some and none
import scala.reflect.runtime.universe._

object Util {
  def filterLeft[F[_], A, B]: Pipe[F, Either[A, B], B] = _.flatMap {
    case Right(r) => Stream.emit(r)
    case Left(_) => Stream.empty
  }

  def filterRight[F[_], A, B]: Pipe[F, Either[A, B], A] = _.flatMap {
    case Left(e) => Stream.emit(e)
    case Right(_) => Stream.empty
  }

  private def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  def getCCFieldNames[A: TypeTag]: List[String] = {
    val members = classAccessors[A]

    members.map(_.name.toString).reverse
  }

  def delimitedStringToMap[A: TypeTag](str: Option[String], delim: Char = '^'): Map[String, String] = {
    val h = getCCFieldNames[A]

    val e = str.fold(List[String]())(_.split(delim).toList)
      .padTo(h.length, "")

    (h zip e).toMap
  }
}
