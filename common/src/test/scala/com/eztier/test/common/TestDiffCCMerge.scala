package com.eztier
package test.common

import org.specs2.mutable._
import common._

class TestDiffCCMerge extends Specification {

  "Arbitrary case classes" should {

    import mergeSyntax._

    "Two case classes of the different type merge" in {

      case class Foo(i: Int, s: String, b: Boolean)
      case class Bar(b: Boolean, s: String)

      val foo = Foo(23, "foo", true)
      val bar = Bar(false, "bar")

      val merged = foo merge bar

      merged mustEqual Foo(23, "bar", false)
    }

    "Two case classes with Option fields can merge" in {
      case class Foo(i: Option[Int], s: String, b: Option[Boolean])
      case class Bar(i: Option[Int], b: Option[Boolean], s: String)

      val foo = Foo(None, "bar", Some(true))
      val bar = Bar(Some(23), Some(false), "")

      val merged = foo merge bar

      merged mustEqual Foo(Some(23), "", Some(false))
    }

  }
}
