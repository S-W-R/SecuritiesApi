package swr.datamodels.security.responses

object ImportSecuritiesResponses {
  final case class ImportSecuritiesResponse(success: List[Int], failed: List[Int])
}
