package com.eztier.testmonocle

import cats.effect.{Async, Blocker, Bracket, ContextShift, ExitCode, IO, IOApp, Resource, Sync}

import monocle.macros.syntax.lens._, monocle.std.option._
import monocle.macros.GenLens
  
case class Object5(v4: Option[String], v5: Int)
case class Object3(object4: Option[Object5], v3: Option[String])
case class Object2(v: String, v2: String)
case class Object1(object2: Object2, object3: Option[Object3])

object App extends IOApp {
  
  val object5 = Object5(None, 5)
  val object3 = Object3(Some(object5), Some("Happy"))
  val object1 = Object1(Object2("", ""), Some(object3))

  val maybeObject3 = GenLens[Object1](_.object3)
  val maybeObject5 = GenLens[Object3](_.object4)
  val maybeString = GenLens[Object5](_.v4)
  
  /*
  (Object1.lens(_.object3) composePrism some composeLens Object3._v3 composePrism some).set("test")(object1)
  (_maybeBar composePrism some composeLens _maybeStr composePrism some).set("Modified String")(myFooInstance)
  */
  
  val lens1 = (maybeObject3 composePrism some composeLens maybeObject5 composePrism some composeLens maybeString composePrism some)
  lens1.getOption(object1)
  lens1.set("Joy")(object1) // Will not work because Object5.v4 was None.
  
  val lens2 = (maybeObject3 composePrism some composeLens maybeObject5 composePrism some composeLens maybeString)
  lens2.getOption(object1)
  lens2.set(Some("OK"))(object1) // Will work.
  
  override def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)
}

