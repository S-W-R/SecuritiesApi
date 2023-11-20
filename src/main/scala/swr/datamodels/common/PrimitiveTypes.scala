package swr.datamodels.common

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative

object PrimitiveTypes {
  type NonNegativeInt = Int Refined NonNegative
}
