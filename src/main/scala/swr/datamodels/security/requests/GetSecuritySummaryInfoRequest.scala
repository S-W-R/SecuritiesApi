package swr.datamodels.security.requests

import swr.datamodels.common.PaginationOptions

final case class GetSecuritySummaryInfoRequest(id: Int, paginationOptions: Option[PaginationOptions])
