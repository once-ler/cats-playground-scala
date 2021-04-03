// Generated by <a href="http://scalaxb.org/">scalaxb</a>.
package soapenvelope11
    
/**
usage:
val obj = scalaxb.fromXML[soapenvelope11.Foo](node)
val document = scalaxb.toXML[soapenvelope11.Foo](obj, "foo", soapenvelope11.defaultScope)
**/
object `package` extends XMLProtocol { }

trait XMLProtocol extends scalaxb.XMLStandardTypes {
  val defaultScope = scalaxb.toScope(Some("tns") -> "http://schemas.xmlsoap.org/soap/envelope/",
    Some("xs") -> "http://www.w3.org/2001/XMLSchema",
    Some("xsi") -> "http://www.w3.org/2001/XMLSchema-instance")
  implicit lazy val Soapenvelope11EnvelopeFormat: scalaxb.XMLFormat[soapenvelope11.Envelope] = new DefaultSoapenvelope11EnvelopeFormat {}
  implicit lazy val Soapenvelope11HeaderFormat: scalaxb.XMLFormat[soapenvelope11.Header] = new DefaultSoapenvelope11HeaderFormat {}
  implicit lazy val Soapenvelope11BodyFormat: scalaxb.XMLFormat[soapenvelope11.Body] = new DefaultSoapenvelope11BodyFormat {}
  implicit lazy val Soapenvelope11FaultFormat: scalaxb.XMLFormat[soapenvelope11.Fault] = new DefaultSoapenvelope11FaultFormat {}
  implicit lazy val Soapenvelope11DetailFormat: scalaxb.XMLFormat[soapenvelope11.Detail] = new DefaultSoapenvelope11DetailFormat {}
  implicit lazy val Soapenvelope11EncodingStyleFormat: scalaxb.AttributeGroupFormat[soapenvelope11.EncodingStyle] = new DefaultSoapenvelope11EncodingStyleFormat {}

  trait DefaultSoapenvelope11EnvelopeFormat extends scalaxb.ElemNameParser[soapenvelope11.Envelope] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    override def typeName: Option[String] = Some("Envelope")

    def parser(node: scala.xml.Node, stack: List[scalaxb.ElemName]): Parser[soapenvelope11.Envelope] =
      phrase(opt(scalaxb.ElemName(Some("http://schemas.xmlsoap.org/soap/envelope/"), "Header")) ~ 
      (scalaxb.ElemName(Some("http://schemas.xmlsoap.org/soap/envelope/"), "Body")) ~ 
      rep(any(_.namespace != Some("http://schemas.xmlsoap.org/soap/envelope/"))) ^^
      { case p1 ~ p2 ~ p3 =>
      soapenvelope11.Envelope(p1.headOption map { scalaxb.fromXML[soapenvelope11.Header](_, scalaxb.ElemName(node) :: stack) },
        scalaxb.fromXML[soapenvelope11.Body](p2, scalaxb.ElemName(node) :: stack),
        p3.toSeq map { scalaxb.fromXML[scalaxb.DataRecord[Any]](_, scalaxb.ElemName(node) :: stack) },
        scala.collection.immutable.ListMap((node match {
          case elem: scala.xml.Elem =>
            elem.attributes.toList flatMap {
              
              case scala.xml.UnprefixedAttribute(key, value, _) =>
                List(("@" + key, scalaxb.DataRecord(None, Some(key), value.text)))
              case scala.xml.PrefixedAttribute(pre, key, value, _) =>
                val ns = elem.scope.getURI(pre)
                List(("@{" + ns + "}" + key, scalaxb.DataRecord(Option[String](ns), Some(key), value.text)))
              case _ => Nil
            }
          case _ => Nil
        }): _*)) })
    
    override def writesAttribute(__obj: soapenvelope11.Envelope, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = {
      var attr: scala.xml.MetaData  = scala.xml.Null
      __obj.attributes.toList map {
        case (key, x) => attr = scala.xml.Attribute((x.namespace map { __scope.getPrefix(_) }).orNull, x.key.orNull, x.value.toString, attr) }
      attr
    }

    def writesChildNodes(__obj: soapenvelope11.Envelope, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =
      Seq.concat(__obj.Header map { scalaxb.toXML[soapenvelope11.Header](_, Some("http://schemas.xmlsoap.org/soap/envelope/"), Some("Header"), __scope, false) } getOrElse {Nil},
        scalaxb.toXML[soapenvelope11.Body](__obj.Body, Some("http://schemas.xmlsoap.org/soap/envelope/"), Some("Body"), __scope, false),
        __obj.any flatMap { x => scalaxb.toXML[scalaxb.DataRecord[Any]](x, x.namespace, x.key, __scope, true) })

  }

  trait DefaultSoapenvelope11HeaderFormat extends scalaxb.ElemNameParser[soapenvelope11.Header] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    override def typeName: Option[String] = Some("Header")

    def parser(node: scala.xml.Node, stack: List[scalaxb.ElemName]): Parser[soapenvelope11.Header] =
      phrase(rep(any(_.namespace != Some("http://schemas.xmlsoap.org/soap/envelope/"))) ^^
      { case p1 =>
      soapenvelope11.Header(p1.toSeq map { scalaxb.fromXML[scalaxb.DataRecord[Any]](_, scalaxb.ElemName(node) :: stack) },
        scala.collection.immutable.ListMap((node match {
          case elem: scala.xml.Elem =>
            elem.attributes.toList flatMap {
              
              case scala.xml.UnprefixedAttribute(key, value, _) =>
                List(("@" + key, scalaxb.DataRecord(None, Some(key), value.text)))
              case scala.xml.PrefixedAttribute(pre, key, value, _) =>
                val ns = elem.scope.getURI(pre)
                List(("@{" + ns + "}" + key, scalaxb.DataRecord(Option[String](ns), Some(key), value.text)))
              case _ => Nil
            }
          case _ => Nil
        }): _*)) })
    
    override def writesAttribute(__obj: soapenvelope11.Header, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = {
      var attr: scala.xml.MetaData  = scala.xml.Null
      __obj.attributes.toList map {
        case (key, x) => attr = scala.xml.Attribute((x.namespace map { __scope.getPrefix(_) }).orNull, x.key.orNull, x.value.toString, attr) }
      attr
    }

    def writesChildNodes(__obj: soapenvelope11.Header, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =
      (__obj.any flatMap { x => scalaxb.toXML[scalaxb.DataRecord[Any]](x, x.namespace, x.key, __scope, true) })

  }

  trait DefaultSoapenvelope11BodyFormat extends scalaxb.ElemNameParser[soapenvelope11.Body] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    override def typeName: Option[String] = Some("Body")

    def parser(node: scala.xml.Node, stack: List[scalaxb.ElemName]): Parser[soapenvelope11.Body] =
      phrase(rep(any(_ => true)) ^^
      { case p1 =>
      soapenvelope11.Body(p1.toSeq map { scalaxb.fromXML[scalaxb.DataRecord[Any]](_, scalaxb.ElemName(node) :: stack) },
        scala.collection.immutable.ListMap((node match {
          case elem: scala.xml.Elem =>
            elem.attributes.toList flatMap {
              
              case scala.xml.UnprefixedAttribute(key, value, _) =>
                List(("@" + key, scalaxb.DataRecord(None, Some(key), value.text)))
              case scala.xml.PrefixedAttribute(pre, key, value, _) =>
                val ns = elem.scope.getURI(pre)
                List(("@{" + ns + "}" + key, scalaxb.DataRecord(Option[String](ns), Some(key), value.text)))
              case _ => Nil
            }
          case _ => Nil
        }): _*)) })
    
    override def writesAttribute(__obj: soapenvelope11.Body, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = {
      var attr: scala.xml.MetaData  = scala.xml.Null
      __obj.attributes.toList map {
        case (key, x) => attr = scala.xml.Attribute((x.namespace map { __scope.getPrefix(_) }).orNull, x.key.orNull, x.value.toString, attr) }
      attr
    }

    def writesChildNodes(__obj: soapenvelope11.Body, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =
      (__obj.any flatMap { x => scalaxb.toXML[scalaxb.DataRecord[Any]](x, x.namespace, x.key, __scope, true) })

  }

  trait DefaultSoapenvelope11FaultFormat extends scalaxb.ElemNameParser[soapenvelope11.Fault] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    override def typeName: Option[String] = Some("Fault")

    def parser(node: scala.xml.Node, stack: List[scalaxb.ElemName]): Parser[soapenvelope11.Fault] =
      phrase((scalaxb.ElemName(None, "faultcode")) ~ 
      (scalaxb.ElemName(None, "faultstring")) ~ 
      opt(scalaxb.ElemName(None, "faultactor")) ~ 
      opt(scalaxb.ElemName(None, "detail")) ^^
      { case p1 ~ p2 ~ p3 ~ p4 =>
      soapenvelope11.Fault(scalaxb.fromXML[javax.xml.namespace.QName](p1, scalaxb.ElemName(node) :: stack),
        scalaxb.fromXML[String](p2, scalaxb.ElemName(node) :: stack),
        p3.headOption map { scalaxb.fromXML[String](_, scalaxb.ElemName(node) :: stack) },
        p4.headOption map { scalaxb.fromXML[soapenvelope11.Detail](_, scalaxb.ElemName(node) :: stack) }) })
    
    def writesChildNodes(__obj: soapenvelope11.Fault, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =
      Seq.concat(scalaxb.toXML[javax.xml.namespace.QName](__obj.faultcode, None, Some("faultcode"), __scope, false),
        scalaxb.toXML[String](__obj.faultstring, None, Some("faultstring"), __scope, false),
        __obj.faultactor map { scalaxb.toXML[String](_, None, Some("faultactor"), __scope, false) } getOrElse {Nil},
        __obj.detail map { scalaxb.toXML[soapenvelope11.Detail](_, None, Some("detail"), __scope, false) } getOrElse {Nil})

  }

  trait DefaultSoapenvelope11DetailFormat extends scalaxb.ElemNameParser[soapenvelope11.Detail] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    override def typeName: Option[String] = Some("detail")

    def parser(node: scala.xml.Node, stack: List[scalaxb.ElemName]): Parser[soapenvelope11.Detail] =
      phrase(rep(any(_ => true)) ^^
      { case p1 =>
      soapenvelope11.Detail(p1.toSeq map { scalaxb.fromXML[scalaxb.DataRecord[Any]](_, scalaxb.ElemName(node) :: stack) },
        scala.collection.immutable.ListMap((node match {
          case elem: scala.xml.Elem =>
            elem.attributes.toList flatMap {
              
              case scala.xml.UnprefixedAttribute(key, value, _) =>
                List(("@" + key, scalaxb.DataRecord(None, Some(key), value.text)))
              case scala.xml.PrefixedAttribute(pre, key, value, _) =>
                val ns = elem.scope.getURI(pre)
                List(("@{" + ns + "}" + key, scalaxb.DataRecord(Option[String](ns), Some(key), value.text)))
              case _ => Nil
            }
          case _ => Nil
        }): _*)) })
    
    override def writesAttribute(__obj: soapenvelope11.Detail, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = {
      var attr: scala.xml.MetaData  = scala.xml.Null
      __obj.attributes.toList map {
        case (key, x) => attr = scala.xml.Attribute((x.namespace map { __scope.getPrefix(_) }).orNull, x.key.orNull, x.value.toString, attr) }
      attr
    }

    def writesChildNodes(__obj: soapenvelope11.Detail, __scope: scala.xml.NamespaceBinding): Seq[scala.xml.Node] =
      (__obj.any flatMap { x => scalaxb.toXML[scalaxb.DataRecord[Any]](x, x.namespace, x.key, __scope, true) })

  }

  trait DefaultSoapenvelope11EncodingStyleFormat extends scalaxb.AttributeGroupFormat[soapenvelope11.EncodingStyle] {
    val targetNamespace: Option[String] = Some("http://schemas.xmlsoap.org/soap/envelope/")
    
    def reads(seq: scala.xml.NodeSeq, stack: List[scalaxb.ElemName]): Either[String, soapenvelope11.EncodingStyle] = seq match {
      case node: scala.xml.Node => Right(soapenvelope11.EncodingStyle((node \ "@{http://schemas.xmlsoap.org/soap/envelope/}encodingStyle").headOption map { scalaxb.fromXML[Seq[java.net.URI]](_, scalaxb.ElemName(node) :: stack) }))
      case _ => Left("reads failed: seq must be scala.xml.Node")
    }
    
    def toAttribute(__obj: soapenvelope11.EncodingStyle, __attr: scala.xml.MetaData, __scope: scala.xml.NamespaceBinding): scala.xml.MetaData = {
      var attr: scala.xml.MetaData  = __attr
      __obj.tnsencodingStyle foreach { x => attr = scala.xml.Attribute(__scope.getPrefix("http://schemas.xmlsoap.org/soap/envelope/"), "encodingStyle", x.map(x => x.toString).mkString(" "), attr) }
      attr
    }
  }


}

