package swr.datamodels.history

import java.time.LocalDate

final case class HistoryRecord(
    securityId: String,
    tradeDate: LocalDate,
    numTrades: BigDecimal, //column name="NUMTRADES" type="double"
    openPrice: Option[BigDecimal],
    closePrice: Option[BigDecimal]
)
