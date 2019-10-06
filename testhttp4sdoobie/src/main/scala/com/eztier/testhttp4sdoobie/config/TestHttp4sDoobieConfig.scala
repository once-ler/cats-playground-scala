package com.eztier.testhttp4sdoobie.config

final case class ServerConfig(host: String, port: Int)
final case class TestHttp4sDoobieConfig(db: DatabaseConfig, server: ServerConfig)
