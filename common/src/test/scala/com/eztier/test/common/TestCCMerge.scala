package com.eztier
package test.common

import org.specs2.mutable._
import cats.implicits._

import common._
import CCMergeSyntax._

class TestCaseClassMerge extends Specification {
  "CCMerge" should {
    "Two case classes of the same type merge" in {

      case class Foo(a: Option[Int], b: List[Int], c: Option[Int])

      val base = Foo(None, Nil, Some(0))
      val update = Foo(Some(3), List(4), None)

      val merged = base merge update // Foo(Some(3),List(4),Some(0))

      merged mustEqual Foo(Some(3), List(4), Some(0))
    }

    "Vectors should be supported" in {
      case class Bar(a: Vector[Int])
      val base = Bar(Vector(1))
      val update = Bar(Vector(2))

      val merged = base merge update

      merged mustEqual Bar(Vector(2, 1))
    }

  }
}
