package swr

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IO.asyncForIO
import cats.effect.IOApp

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import swr.configuration.AppConfig
import swr.infrastructure.db.DBMigrations
import swr.server.HttpServer

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
    for {
      config <- AppConfig.load[IO]()

      _ <- DBMigrations.migrate(config.jdbcDatabaseConfig)

      exitCode <- HttpServer
        .run[IO](config)
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }
}
