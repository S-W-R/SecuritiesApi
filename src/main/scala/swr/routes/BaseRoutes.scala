package swr.routes

import java.time.LocalDate

import sttp.tapir.*

import swr.datamodels.history.HistoryRecordId

object BaseRoutes {
  val Version: String = "v1"

  val baseEndpointV1: PublicEndpoint[Unit, Unit, Unit, Any] = endpoint.in("securities" / Version)

  val historyIdInput: EndpointInput[HistoryRecordId] = path[String]("secId")
    .and(path[LocalDate]("date"))
    .map(input => HistoryRecordId(input._1, input._2))(id => (id.securityId, id.tradeDate))
}
