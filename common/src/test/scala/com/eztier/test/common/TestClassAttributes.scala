package com.eztier
package test.common

import org.specs2.mutable._
import common._
import Util._

case class User(id: Long, firstName: String, lastName: String, role: String)

class TestClassAttributes extends Specification {

  import scala.reflect.runtime.universe.TypeTag

  def toCkTypeName[A](implicit typeTag: TypeTag[A]) = {
    val tname = typeTag.tpe.getClass.getSimpleName
    tname match {
      case a if a == "CkPostalContactInformation" => "Postal Contact Information"
      case a if a == "CkPhoneContactInformation" => "Phone Contact Information"
      case a if a == "CkEmailContactInformation" => "E-mail Contact Information"
      case _ => tname.replace("Ck", "")
    }
  }

  "Case class attributes" should {
    "Get extracted" in {
      val keys = Attributes[User].fieldNames

      val keys2 = getCCFieldNames[User]

      keys mustEqual keys2
    }
  }

  "Type" should {
    "Convert to simple string" in {
      val tname = TypeInfo[User].name

      val uname = toCkTypeName[User]

      tname mustEqual uname
    }
  }

}
