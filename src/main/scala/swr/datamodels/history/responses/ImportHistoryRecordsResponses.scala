package swr.datamodels.history.responses

import swr.datamodels.history.HistoryRecordId

object ImportHistoryRecordsResponses {
  final case class ImportHistoryRecordsResponse(success: List[HistoryRecordId], failed: List[HistoryRecordId])
}
