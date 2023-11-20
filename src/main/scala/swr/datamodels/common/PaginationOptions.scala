package swr.datamodels.common

import swr.datamodels.common.PrimitiveTypes.NonNegativeInt

final case class PaginationOptions(limit: Option[NonNegativeInt], offset: Option[NonNegativeInt])
