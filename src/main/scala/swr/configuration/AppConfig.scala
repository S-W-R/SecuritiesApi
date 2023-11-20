package swr.configuration

import cats.effect.Sync
import cats.syntax.all.*

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

final case class AppConfig(serverConfig: ServerConfig, jdbcDatabaseConfig: JdbcDatabaseConfig)

object AppConfig { // pureconfig не завезли в третью скалу
  def load[F[_]: Sync](): F[AppConfig] =
    Sync[F].delay {
      implicit val globalConfig: Config = ConfigFactory.load()
      AppConfig(
        ServerConfig.parseFromGlobalConfig,
        JdbcDatabaseConfig.parseFromGlobalConfig
      )
    }
}
