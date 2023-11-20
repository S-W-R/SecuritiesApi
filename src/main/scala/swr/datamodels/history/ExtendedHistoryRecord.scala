package swr.datamodels.history

import java.time.LocalDate
final case class ExtendedHistoryRecord(
    securityId: String,
    name: String,
    registrationNumber: String,
    emitentTitle: String,
    tradeDate: LocalDate,
    numTrades: BigDecimal,
    openPrice: Option[BigDecimal],
    closePrice: Option[BigDecimal]
)
