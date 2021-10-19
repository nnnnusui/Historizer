package com.github.nnnnusui.historizer

import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}
import com.typesafe.config.ConfigFactory

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object Https extends App {
  def context: HttpsConnectionContext = {
    val config = ConfigFactory.load()
    val sslKeyConfig = new {
      private val root     = config.getConfig("ssl-key")
      val path: String     = root.getString("path")
      val password: String = root.getString("password")
      val `type`: String   = root.getString("type")
    }
    val password: Array[Char] = sslKeyConfig.password.toCharArray

    val keyStore: KeyStore = KeyStore.getInstance(sslKeyConfig.`type`)
    keyStore.load(getClass.getClassLoader.getResourceAsStream(sslKeyConfig.path), password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password)

    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(
      keyManagerFactory.getKeyManagers,
      trustManagerFactory.getTrustManagers,
      new SecureRandom
    )
    ConnectionContext.httpsServer(sslContext)
  }
}
