package swr.datamodels.history.responses

import java.util.UUID

object PutHistoryRecordResponses {
  final case class PutHistoryRecordResponse(id: UUID)

  sealed trait PutHistoryRecordError
  case object HistoryRecordAlreadyExists extends PutHistoryRecordError
  case object SecurityNotFound extends PutHistoryRecordError
}
