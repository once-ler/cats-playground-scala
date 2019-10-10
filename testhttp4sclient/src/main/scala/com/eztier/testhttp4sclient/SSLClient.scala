package com.eztier.testhttp4sclient

// https://github.com/http4s/http4s/blob/master/examples/src/main/scala/com/example/http4s/ssl.scala

import org.http4s.client.blaze.{BlazeClientBuilder}
import java.io.{ByteArrayInputStream, File}
import java.nio.file.Files
import java.security.{KeyFactory, KeyStore, PrivateKey, SecureRandom}
import java.security.cert.{Certificate, CertificateFactory}
import java.security.spec.PKCS8EncodedKeySpec

import cats.effect.IO
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object Test {
  val caString = """
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
"""

  val certString = """
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
"""

  // Prepare certificates (used for both trust and client auth)
  def certStream(cert: String) = new ByteArrayInputStream(cert.getBytes("UTF-8"))
  val cf = CertificateFactory.getInstance("X.509")
  val ca: Certificate = cf.generateCertificate(certStream(caString))
  val cert: Certificate = cf.generateCertificate(certStream(certString))


  // Get private key for client auth
  val keyBytes = Files.readAllBytes(new File(getClass.getResource("/etcd-ssl-key.der").toURI).toPath)
  val spec = new PKCS8EncodedKeySpec(keyBytes)
  val kf = KeyFactory.getInstance("RSA")
  val pk: PrivateKey = kf.generatePrivate(spec)

  // Key store used for client auth
  val ksK: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType)
  ksK.load(null)
  ksK.setKeyEntry("etcd", pk, Array.empty, Array(cert, ca))

  // Key manager used for client auth
  val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
  kmf.init(ksK, Array.empty)

  // Key store used to trust server cert
  val ksT: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType)
  ksT.load(null)
  ksT.setCertificateEntry("etcd-ca", ca)

  // Key manager used for trusting server cert
  val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
  tmf.init(ksT)

  // Set up SSL context
  val ssl = SSLContext.getInstance("TLS")
  ssl.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom())

  val ec = scala.concurrent.ExecutionContext.global
  implicit val cs = IO.contextShift(ec)

  val client = BlazeClientBuilder[IO](ec).withSslContext(ssl).resource.use{
    _.expect[String]("https://127.0.0.1:3009/health")
  }.map{
    println(_)
  }

}


