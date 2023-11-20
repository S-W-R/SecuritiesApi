package swr.datamodels.history

import java.time.LocalDate

final case class HistoryRecordId(securityId: String, tradeDate: LocalDate)
