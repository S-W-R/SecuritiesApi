package swr.server

import scala.concurrent.ExecutionContext

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*

import com.comcast.ip4s.*
import doobie.Transactor
import fs2.io.net.Network
import fs2.io.net.Network.forAsync
import org.http4s.ember.server.*
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

import swr.configuration.AppConfig
import swr.configuration.JdbcDatabaseConfig
import swr.infrastructure.db.DBTransactor
import swr.repositories.HistoryRepository
import swr.repositories.SecuritiesRepository
import swr.routes.HistoryRoutes
import swr.routes.ImportDataRoutes
import swr.routes.SecuritiesRoutes
import swr.routes.SummaryDataRoutes
import swr.routes.SwaggerRoutes
import swr.services.HistoryService
import swr.services.SecuritiesService
import swr.services.SummaryDataService

object HttpServer {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def run[F[_] : Async : Logger](appConfig: AppConfig): Resource[F, Server] = {
    given JdbcDatabaseConfig = appConfig.jdbcDatabaseConfig // todo: provider

    val serverConfig = appConfig.serverConfig

    given Transactor[F] = DBTransactor.instance[F] // implicit0 when downgrading to scala2

    given SecuritiesRepository[F] = SecuritiesRepository[F]

    given HistoryRepository[F] = HistoryRepository[F]

    given SecuritiesService[F] = SecuritiesService[F]

    given HistoryService[F] = HistoryService[F]

    given SummaryDataService[F] = SummaryDataService[F]

    val routes = SwaggerRoutes[F]
      <+> SecuritiesRoutes[F]
      <+> HistoryRoutes[F]
      <+> SummaryDataRoutes[F]
      <+> ImportDataRoutes[F]

    given Network[F] = forAsync

    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(serverConfig.host).getOrElse(host"127.0.0.1"))
      .withPort(Port.fromString(serverConfig.port).getOrElse(port"8080"))
      .withHttpApp(routes.orNotFound)
      .build
  }
}
