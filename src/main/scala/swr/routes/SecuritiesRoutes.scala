package swr.routes

import cats.effect.Async

import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import swr.datamodels.security.Security
import swr.datamodels.security.responses.DeleteSecurityResponses.DeleteSecurityError
import swr.datamodels.security.responses.PutSecurityResponses.PutSecurityError
import swr.routes.BaseRoutes.baseEndpointV1
import swr.services.SecuritiesService

object SecuritiesRoutes {
  private val securityBaseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    baseEndpointV1.in("securities").tag("securities")

  private val getSecurityEndpoint: PublicEndpoint[Int, Unit, Option[Security], Any] =
    securityBaseEndpoint.get
      .in(path[Int]("id"))
      .out(jsonBody[Option[Security]])

  private val putSecurityEndpoint: PublicEndpoint[Security, PutSecurityError, Unit, Any] =
    securityBaseEndpoint.put
      .in(jsonBody[Security])
      .errorOut(jsonBody[PutSecurityError])

  private val deleteSecurityEndpoint: PublicEndpoint[Int, DeleteSecurityError, Unit, Any] =
    securityBaseEndpoint.delete
      .in(path[Int]("id"))
      .errorOut(jsonBody[DeleteSecurityError])

  val endpoints: List[AnyEndpoint] =
    getSecurityEndpoint
      :: putSecurityEndpoint
      :: deleteSecurityEndpoint
      :: Nil

  def apply[F[_]: Async](implicit securityService: SecuritiesService[F]): HttpRoutes[F] = {
    val serverEndpoints: List[ServerEndpoint[Fs2Streams[F], F]] = List(
      getSecurityEndpoint.serverLogicSuccess(securityService.getSecurity),
      putSecurityEndpoint.serverLogic(securityService.putSecurity),
      deleteSecurityEndpoint.serverLogic(securityService.deleteSecurity)
    )

    Http4sServerInterpreter[F]().toRoutes(serverEndpoints)
  }
}
