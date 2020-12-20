package com.eztier
package common

import java.net.URI
import scalaxb.{DispatchHttpClientsAsync, Soap11ClientsAsync}
import scala.concurrent.ExecutionContext

trait UsernameTokenSoap11ClientAsync extends Soap11ClientsAsync with DispatchHttpClientsAsync {
  def url: Option[String]
  def user: Option[String]
  def pass: Option[String]

  private def getWssUsernameTokenHeader(user: String, pass: String) =
    s"""
    <soapenv:Header xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
      <wsse:Security soap:mustUnderstand="1" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
        <wsse:UsernameToken wsu:Id="UsernameToken-1" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
          <wsse:Username>$user</wsse:Username>
          <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">$pass</wsse:Password>
        </wsse:UsernameToken>
      </wsse:Security>
    </soapenv:Header>"""

  override lazy val httpClient: DispatchHttpClient with Object = new DispatchHttpClient {
    override def request(in: String, address: URI, headers: Map[String, String])(implicit ec: ExecutionContext) = {
      val wssecurity = getWssUsernameTokenHeader(user.getOrElse(""), pass.getOrElse(""))
      val in2 = in.replace("<soap11:Body>", wssecurity + "<soap11:Body>")

      super.request(in2, address, headers)
    }
  }
}