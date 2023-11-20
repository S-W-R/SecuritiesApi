package swr.routes

import java.time.LocalDate

import cats.effect.Async

import eu.timepit.refined
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

import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.HistoryRecordId
import swr.datamodels.history.requests.SearchHistoriesBySecIdRequest
import swr.datamodels.history.responses.DeleteHistoryRecordResponses.DeleteHistoryRecordError
import swr.datamodels.history.responses.PutHistoryRecordResponses.PutHistoryRecordError
import swr.routes.BaseRoutes.baseEndpointV1
import swr.routes.BaseRoutes.historyIdInput
import swr.services.HistoryService

object HistoryRoutes {
  private val historyRecordsEndpoints: PublicEndpoint[Unit, Unit, Unit, Any] =
    baseEndpointV1.in("history").tag("history")

  private val getHistoryRecordEndpoint: PublicEndpoint[HistoryRecordId, Unit, Option[HistoryRecord], Any] =
    historyRecordsEndpoints.get
      .in(historyIdInput)
      .out(jsonBody[Option[HistoryRecord]])

  private val putHistoryRecordEndpoint: PublicEndpoint[HistoryRecord, PutHistoryRecordError, Unit, Any] =
    historyRecordsEndpoints.put
      .in(jsonBody[HistoryRecord])
      .errorOut(jsonBody[PutHistoryRecordError])

  private val deleteHistoryRecordEndpoint: PublicEndpoint[HistoryRecordId, DeleteHistoryRecordError, Unit, Any] =
    historyRecordsEndpoints.delete
      .in(historyIdInput)
      .errorOut(jsonBody[DeleteHistoryRecordError])

  private val searchHistoriesBySecIdEndpoint
      : PublicEndpoint[SearchHistoriesBySecIdRequest, Unit, List[HistoryRecord], Any] =
    historyRecordsEndpoints
      .in("searchBySecId")
      .post
      .in(jsonBody[SearchHistoriesBySecIdRequest])
      .out(jsonBody[List[HistoryRecord]])

  val endpoints: List[AnyEndpoint] =
    getHistoryRecordEndpoint :: putHistoryRecordEndpoint :: deleteHistoryRecordEndpoint :: searchHistoriesBySecIdEndpoint :: Nil

  def apply[F[_]: Async](implicit historyService: HistoryService[F]): HttpRoutes[F] = {
    val serverEndpoints: List[ServerEndpoint[Fs2Streams[F], F]] = List(
      getHistoryRecordEndpoint.serverLogicSuccess(historyService.getHistoryRecord),
      putHistoryRecordEndpoint.serverLogic(historyService.putHistoryRecord),
      deleteHistoryRecordEndpoint.serverLogic(historyService.deleteHistoryRecord),
      searchHistoriesBySecIdEndpoint.serverLogicSuccess(historyService.searchHistoriesBySecId)
    )
    Http4sServerInterpreter[F]().toRoutes(serverEndpoints)
  }
}
