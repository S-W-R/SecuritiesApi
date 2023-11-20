package swr.datamodels.history.responses

object DeleteHistoryRecordResponses {
  sealed trait DeleteHistoryRecordError

  case object HistoryRecordDoesNotExist extends DeleteHistoryRecordError
}
