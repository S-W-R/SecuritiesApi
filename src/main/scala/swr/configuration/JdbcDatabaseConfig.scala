package swr.configuration

import scala.jdk.CollectionConverters.*

import com.typesafe.config.Config

final case class JdbcDatabaseConfig(
    url: String,
    driver: String,
    user: String,
    password: String,
    migrationsLocations: List[String]
)

object JdbcDatabaseConfig {
  def parseFromGlobalConfig(implicit globalConfig: Config): JdbcDatabaseConfig = {
    val config = globalConfig.getConfig("jdbc")
    JdbcDatabaseConfig(
      config.getString("url"),
      config.getString("driver"),
      config.getString("user"),
      config.getString("password"),
      config.getStringList("migrationsLocations").asScala.toList
    )
  }
}
