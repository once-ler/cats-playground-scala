package com.eztier
package test.common

import org.specs2.mutable._
import common._
import Util._

class TestClassAttributes extends Specification {

  "Case class attributes" should {
    "Get extracted" in {
       case class User(id: Long, firstName: String, lastName: String, role: String)

       val keys = Attributes[User].fieldNames

       val keys2 = getCCFieldNames[User]

       keys mustEqual keys2
    }
 }

}
