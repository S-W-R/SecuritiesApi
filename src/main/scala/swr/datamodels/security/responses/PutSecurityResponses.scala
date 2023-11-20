package swr.datamodels.security.responses

import java.util.UUID

object PutSecurityResponses {
  final case class PutSecurityResponse(id: UUID)
  
  sealed trait PutSecurityError
  case object SecurityAlreadyExists extends PutSecurityError
}
