package com.eztier
package common

import fs2.{Pipe, Stream}
import cats.syntax.option._

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

    members.map(_.name.toString)
  }

  def delimitedStringToMap[A: TypeTag](str: Option[String], delim: Char = '^'): Map[String, String] = {
    val h = getCCFieldNames[A]

    val e = str.fold(List[String]())(_.split(delim).toList)
      .padTo(h.length, "")

    (h zip e).toMap
  }

  // Empty string becomes Some(), we want None.
  implicit class OptionEmptyStringToNone(fa: Option[String]) {
    def toNoneIfEmpty = fa.flatMap(a => if (a.length == 0) None else Some(a))
  }

  def csvToCC[A](converter: CSVConverter[List[A]], str: Option[String], default: A) = {
    converter.from(str.fold("")(a => a))
      .fold(e => List[A](), s => s)
      .headOption.fold(default)(a => a)
  }

}
