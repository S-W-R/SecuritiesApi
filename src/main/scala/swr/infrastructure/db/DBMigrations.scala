package swr.infrastructure.db

import scala.jdk.CollectionConverters.*

import cats.effect.Sync
import cats.implicits.*

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

import swr.configuration.JdbcDatabaseConfig

object DBMigrations {

  def migrate[F[_]: Sync](config: JdbcDatabaseConfig)(implicit logger: Logger[F]): F[Int] =
    for {
      _ <- Logger[F].info(
        "Running migrations from locations: " +
          config.migrationsLocations.mkString(", ")
      )
      count <- unsafeMigrate(config)
      _ <- info"Executed $count migrations"
    } yield count

  private def unsafeMigrate[F[_]: Sync: Logger](config: JdbcDatabaseConfig): F[Int] = Sync[F].delay {
    val m: FluentConfiguration = Flyway.configure
      .dataSource(
        config.url,
        config.user,
        config.password
      )
      .group(true)
      .outOfOrder(false)
      .locations(
        config.migrationsLocations
          .map(new Location(_)): _*
      )
      .baselineOnMigrate(true)

    logValidationErrorsIfAny(m)
    m.load().migrate().migrationsExecuted
  }

  private def logValidationErrorsIfAny[F[_]: Sync: Logger](m: FluentConfiguration): F[Unit] = Sync[F].delay {
    val validated = m
      .load()
      .validateWithResult()

    if (!validated.validationSuccessful)
      for (error <- validated.invalidMigrations.asScala)
        Logger[F].warn(s"""
             |Failed validation:
             |  - version: ${error.version}
             |  - path: ${error.filepath}
             |  - description: ${error.description}
             |  - errorCode: ${error.errorDetails.errorCode}
             |  - errorMessage: ${error.errorDetails.errorMessage}
        """.stripMargin.strip)
  }
}
