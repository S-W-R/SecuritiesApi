package swr.datamodels.security.responses

object DeleteSecurityResponses {
  sealed trait DeleteSecurityError
  case object SecurityDoesNotExist extends DeleteSecurityError
}
