package swr.datamodels.security

import swr.datamodels.history.HistoryRecord

final case class SecuritySummaryInfo(security: Security, history: List[HistoryRecord])
