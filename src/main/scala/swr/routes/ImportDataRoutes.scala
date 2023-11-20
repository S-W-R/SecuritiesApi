package swr.routes

import cats.effect.Async

import io.circe.generic.auto.*
import org.http4s.HttpRoutes
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.responses.ImportHistoryRecordsResponses.ImportHistoryRecordsResponse
import swr.datamodels.security.Security
import swr.datamodels.security.responses.ImportSecuritiesResponses.ImportSecuritiesResponse
import swr.infrastructure.codecs.ImportCodec.*
import swr.routes.BaseRoutes.baseEndpointV1
import swr.services.HistoryService
import swr.services.SecuritiesService

object ImportDataRoutes {
  private val importDataBaseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    baseEndpointV1.in("import").tag("import")

  private val importSecuritiesEndpoint: PublicEndpoint[List[Security], Unit, ImportSecuritiesResponse, Any] =
    importDataBaseEndpoint.post
      .in("securities")
      .in(xmlBody[List[Security]])
      .out(jsonBody[ImportSecuritiesResponse])
      .description("Example: https://iss.moex.com/iss/securities.xml")

  private val importHistoryEndpoint: PublicEndpoint[List[HistoryRecord], Unit, ImportHistoryRecordsResponse, Any] =
    importDataBaseEndpoint.post
      .in("history")
      .in(xmlBody[List[HistoryRecord]])
      .out(jsonBody[ImportHistoryRecordsResponse])
      .description(
        "Example: https://iss.moex.com/iss/history/engines/stock/markets/shares/boards/tqbr/securities.xml?date=2013-12-20"
      )

  val endpoints: List[AnyEndpoint] = importSecuritiesEndpoint :: importHistoryEndpoint :: Nil

  def apply[F[_]: Async](implicit
      securityService: SecuritiesService[F],
      historyService: HistoryService[F]
  ): HttpRoutes[F] = {
    val serverEndpoints: List[ServerEndpoint[Fs2Streams[F], F]] = List(
      importSecuritiesEndpoint.serverLogicSuccess(securityService.importSecurities),
      importHistoryEndpoint.serverLogicSuccess(historyService.importHistoryRecords)
    )

    Http4sServerInterpreter[F]().toRoutes(serverEndpoints)
  }
}
