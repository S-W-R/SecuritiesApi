package swr.datamodels.history.requests

import io.circe.generic.semiauto.*

import swr.datamodels.common.PaginationOptions

final case class SearchHistoriesBySecIdRequest(
    securityId: String,
    paginationOptions: Option[PaginationOptions]
)
