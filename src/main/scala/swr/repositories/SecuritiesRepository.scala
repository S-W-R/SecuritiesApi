package swr.repositories

import cats.effect.Sync
import cats.syntax.all.*

import doobie.*
import doobie.implicits.*
import doobie.postgres.*

import swr.datamodels.security.Security
import swr.datamodels.security.responses.DeleteSecurityResponses
import swr.datamodels.security.responses.DeleteSecurityResponses.DeleteSecurityError
import swr.datamodels.security.responses.DeleteSecurityResponses.SecurityDoesNotExist
import swr.datamodels.security.responses.PutSecurityResponses
import swr.datamodels.security.responses.PutSecurityResponses.PutSecurityError
import swr.datamodels.security.responses.PutSecurityResponses.SecurityAlreadyExists

trait SecuritiesRepository[F[_]: Sync] {
  def getSecurityById(id: Int): F[Option[Security]]

  def getSecurityBySecId(secId: String): F[Option[Security]]

  def putSecurity(security: Security): F[Either[PutSecurityError, Unit]]

  def deleteSecurity(id: Int): F[Either[DeleteSecurityError, Unit]]
}

object SecuritiesRepository {
  def apply[F[_]: Sync](implicit xa: Transactor[F]): SecuritiesRepository[F] = new SecuritiesRepository[F] {

    override def getSecurityById(id: Int): F[Option[Security]] =
      sql"select $fullSecurityFields from $securitiesTableName where id = $id"
        .query[Security]
        .option
        .transact(xa)

    override def getSecurityBySecId(secId: String): F[Option[Security]] =
      sql"select $fullSecurityFields from $securitiesTableName where sec_id = $secId"
        .query[Security]
        .option
        .transact(xa)

    override def putSecurity(security: Security): F[Either[PutSecurityError, Unit]] =
      fr"insert into $securitiesTableName ($fullSecurityFields) values(${security.id},${security.securityId}, ${security.name}, ${security.registrationNumber}, ${security.emitentTitle})".update.run
        .map(_ => ())
        .transact(xa)
        .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => SecurityAlreadyExists }

    override def deleteSecurity(id: Int): F[Either[DeleteSecurityError, Unit]] =
      sql"delete from $securitiesTableName where id = $id".update.run
        .transact(xa)
        .map {
          case 0 => SecurityDoesNotExist.asLeft[Unit]
          case _ => Right[DeleteSecurityError, Unit](())
        }

    private val securitiesTableName = fr"securities"
    private val insertSecurityFields = fr"sec_id, sec_name, regnumber, emitent_title"
    private val fullSecurityFields = fr"id, " ++ insertSecurityFields
  }
}
