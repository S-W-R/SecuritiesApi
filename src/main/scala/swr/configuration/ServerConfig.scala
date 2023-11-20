package swr.configuration

import com.typesafe.config.Config

final case class ServerConfig(host: String, port: String)

object ServerConfig {
  def parseFromGlobalConfig(implicit globalConfig: Config): ServerConfig = {
    val config = globalConfig.getConfig("server")
    ServerConfig(config.getString("host"), config.getString("port"))
  }
}
