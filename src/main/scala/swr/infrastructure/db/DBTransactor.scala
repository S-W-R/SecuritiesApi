package swr.infrastructure.db

import cats.effect.Async

import doobie.*

import swr.configuration.JdbcDatabaseConfig

object DBTransactor {
  def instance[F[_]: Async](implicit jdbcDatabaseConfig: JdbcDatabaseConfig): Transactor[F] =
    doobie.Transactor.fromDriverManager[F](
      driver = jdbcDatabaseConfig.driver,
      url = jdbcDatabaseConfig.url,
      user = jdbcDatabaseConfig.user,
      password = jdbcDatabaseConfig.password,
      logHandler = None
    )
}
