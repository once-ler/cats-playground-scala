package com.eztier.clickmock
package domain

import java.util.Date

import cats.{Applicative, Functor, Monad}
import cats.effect.{Async, Bracket, Concurrent, ContextShift, IO, Sync}
import fs2.Pipe

import scala.xml.XML
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.xml.{Elem, Node, NodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import fs2.Stream

trait CkBase {
  val oid: Option[String]
  val Class: Option[String]
  val extent: Option[String]
}

trait WithCustomAttributes {
  val customAttributes: Option[EntityReference[_]]
}

trait WithProject {
  val _webrUnique_ID: Option[String]
}

trait WithNonProject {
  val ID: Option[String]
}

trait WithFindById {
  def findById(id: Option[String]): Option[_]
}

trait WithFindByMrn {
  def findByMrn(mrn: Option[String]): Option[_]
}

trait WithExplicitTypeName {
  def typeName: Option[String]
}

trait WithEncoder {
  this: CkBase =>
  def encoder(name: String, el: Any) = {
    val in = el match {
      case a: String => <string value={a}></string>
      case a: Boolean => <boolean value={a.toString}></boolean>
      case a: Int => <integer value={a.toString}></integer>
      case a: Float => <float value={a.toString}></float>
      case a: Date => <date value={a.getTime.toString}></date>
      case a: EntityReference[_] if a.Poref.getOrElse("").length > 0 => <entityReference type={a.Type} poref={a.Poref}></entityReference>
      case a: PersistentReference if a.Poref.getOrElse("").length > 0 => <persistentReference poref={a.Poref}></persistentReference>
      case _ => <null />
    }
    <attr name={name}>{in}</attr>
  }

  def toXml = {
    val b = (Seq[NodeSeq]() /: this.getClass.getDeclaredFields.filter(f => !f.getName.matches("^\\b(?i)oid|class|extent\\b$"))) {
      (a, f) =>
        f.setAccessible(true)
        val x = encoder(if (f.getName == "_webrUnique_ID") "ID" else f.getName, f.get(this))

        a ++ x
    }
    <mainspan>{b}</mainspan>
  }

  def toCkTypeName = this.getClass.getSimpleName.replace("Ck", "")

  def toOid: String = this.oid.getOrElse("")
}

