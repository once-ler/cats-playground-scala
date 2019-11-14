package com.eztier.clickmock
package domain

import scala.xml.NodeSeq

// com.webridge.entity.Entity:
case class EntityReference[A <: Any](Poref: String = "", Type: String = "")

// com.webridge.eset.EntitySet:
case class PersistentReference(Poref: String = "")

// Use for from NodeSeq to Ck... types
case class WrappedEntityXml(xml: NodeSeq)
case class WrappedEntitySetXml(xml: NodeSeq)

