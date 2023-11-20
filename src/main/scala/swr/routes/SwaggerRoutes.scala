package swr.routes

import cats.effect.Async

import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SwaggerRoutes {
  def apply[F[_]: Async]: HttpRoutes[F] = {
    val endpoints = SecuritiesRoutes.endpoints ++ HistoryRoutes.endpoints ++ SummaryDataRoutes.endpoints ++ ImportDataRoutes.endpoints
    val swaggerEndpoints = SwaggerInterpreter().fromEndpoints[F](endpoints, "SecuritiesApi", BaseRoutes.Version)
    Http4sServerInterpreter[F]().toRoutes(swaggerEndpoints)
  }
}
