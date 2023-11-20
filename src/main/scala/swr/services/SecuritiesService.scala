package swr.services

import scala.language.postfixOps

import cats.effect.Sync
import cats.syntax.all.*

import swr.datamodels.security.Security
import swr.datamodels.security.responses.DeleteSecurityResponses.DeleteSecurityError
import swr.datamodels.security.responses.ImportSecuritiesResponses.ImportSecuritiesResponse
import swr.datamodels.security.responses.PutSecurityResponses.PutSecurityError
import swr.repositories.SecuritiesRepository

trait SecuritiesService[F[_]: Sync] {
  def getSecurity(id: Int): F[Option[Security]]

  def putSecurity(security: Security): F[Either[PutSecurityError, Unit]]

  def deleteSecurity(id: Int): F[Either[DeleteSecurityError, Unit]]

  def importSecurities(securities: List[Security]): F[ImportSecuritiesResponse]

}

object SecuritiesService {
  def apply[F[_]: Sync](implicit securityRepository: SecuritiesRepository[F]): SecuritiesService[F] =
    new SecuritiesService[F] {
      override def getSecurity(id: Int): F[Option[Security]] = securityRepository.getSecurityById(id)

      override def putSecurity(security: Security): F[Either[PutSecurityError, Unit]] =
        securityRepository.putSecurity(security)

      override def deleteSecurity(id: Int): F[Either[DeleteSecurityError, Unit]] = securityRepository.deleteSecurity(id)

      override def importSecurities(securities: List[Security]): F[ImportSecuritiesResponse] =
        securities
          .traverse(x => putSecurity(x).map(res => res.leftMap(_ => x.id).map(_ => x.id)))
          .map(_.partitionMap(identity).swap)
          .map(ImportSecuritiesResponse.apply)
    }
}
