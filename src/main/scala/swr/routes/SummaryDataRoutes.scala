package swr.routes

import cats.effect.Async

import io.circe.generic.auto.*
import io.circe.refined.*
import org.http4s.HttpRoutes
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.codec.refined.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import swr.datamodels.common.PaginationOptions
import swr.datamodels.history.ExtendedHistoryRecord
import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.HistoryRecordId
import swr.datamodels.security.Security
import swr.datamodels.security.SecuritySummaryInfo
import swr.datamodels.security.requests.GetSecuritySummaryInfoRequest
import swr.routes.BaseRoutes.baseEndpointV1
import swr.routes.BaseRoutes.historyIdInput
import swr.services.SummaryDataService

object SummaryDataRoutes {
  private val summaryDataBaseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    baseEndpointV1.in("summary").tag("summary")

  private val getSecuritySummaryInput: EndpointInput[GetSecuritySummaryInfoRequest] = path[Int]("id")
    .and(jsonBody[Option[PaginationOptions]])
    .map(input => GetSecuritySummaryInfoRequest(input._1, input._2))(request => (request.id, request.paginationOptions))

  private val getSecuritySummaryInfoEndpoint
      : PublicEndpoint[GetSecuritySummaryInfoRequest, Unit, Option[SecuritySummaryInfo], Any] =
    summaryDataBaseEndpoint.post
      .in("securities")
      .in(getSecuritySummaryInput)
      .out(jsonBody[Option[SecuritySummaryInfo]])

  private val getExtendedHistoryRecordEndpoint
      : PublicEndpoint[HistoryRecordId, Unit, Option[ExtendedHistoryRecord], Any] =
    summaryDataBaseEndpoint.get
      .in("history")
      .in(historyIdInput)
      .out(jsonBody[Option[ExtendedHistoryRecord]])

  val endpoints: List[AnyEndpoint] = getSecuritySummaryInfoEndpoint :: getExtendedHistoryRecordEndpoint :: Nil

  def apply[F[_]: Async](implicit summaryDataService: SummaryDataService[F]): HttpRoutes[F] = {
    val serverEndpoints: List[ServerEndpoint[Fs2Streams[F], F]] = List(
      getSecuritySummaryInfoEndpoint.serverLogicSuccess(summaryDataService.getSecuritySummaryInfo),
      getExtendedHistoryRecordEndpoint.serverLogicSuccess(summaryDataService.getExtendedHistoryRecord)
    )

    Http4sServerInterpreter[F]().toRoutes(serverEndpoints)
  }
}
