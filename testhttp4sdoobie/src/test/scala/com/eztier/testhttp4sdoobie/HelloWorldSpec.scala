package com.eztier.testhttp4sdoobie

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

// https://http4s.org/v0.20/streaming/

class HelloWorldSpec extends org.specs2.mutable.Specification {
  /*
  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }
    "return hello world" >> {
      uriReturnsHelloWorld()
    }
  }

  private[this] val retHelloWorld: Response[IO] = {
    val getHW = Request[IO](Method.GET, uri"/authors/1")
    val helloWorld = HelloWorld.impl[IO]
    Testhttp4sdoobieRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW).unsafeRunSync()
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)

  // http://localhost:8080/authors
  // Not found
  // // http://localhost:8080/authors/0
  // The author was not found
  // http://localhost:8080/authors/1
  // {"firstName":"Axel","lastName":"Rod","email":"arod@nowhere.org","phone":"212-111-1111","id":1}
  // http://localhost:8080/authors/2
  // {"firstName":"Baby","lastName":"Lilly","email":"blilly@nowhere.org","phone":"212-222-2222","id":2}
  // http://localhost:8080/authors/3
  // {"firstName":"Crazy","lastName":"Horse","email":"chorse@nowhere.org","phone":"212-333-3333","id":3}
  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")
  */

}
