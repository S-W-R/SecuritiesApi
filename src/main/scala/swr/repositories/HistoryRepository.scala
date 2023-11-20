package swr.repositories

import cats.effect.Sync
import cats.syntax.all.*

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.refined.implicits.*
import eu.timepit.refined.api.Refined

import swr.datamodels.common.PrimitiveTypes.NonNegativeInt
import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.HistoryRecordId
import swr.datamodels.history.requests.SearchHistoriesBySecIdRequest
import swr.datamodels.history.responses.DeleteHistoryRecordResponses
import swr.datamodels.history.responses.DeleteHistoryRecordResponses.DeleteHistoryRecordError
import swr.datamodels.history.responses.DeleteHistoryRecordResponses.HistoryRecordDoesNotExist
import swr.datamodels.history.responses.PutHistoryRecordResponses
import swr.datamodels.history.responses.PutHistoryRecordResponses.HistoryRecordAlreadyExists
import swr.datamodels.history.responses.PutHistoryRecordResponses.PutHistoryRecordError
import swr.datamodels.history.responses.PutHistoryRecordResponses.SecurityNotFound

trait HistoryRepository[F[_]: Sync] {
  def getHistoryRecord(id: HistoryRecordId): F[Option[HistoryRecord]]

  def putHistoryRecord(historyRecord: HistoryRecord): F[Either[PutHistoryRecordError, Unit]]

  def deleteHistoryRecord(id: HistoryRecordId): F[Either[DeleteHistoryRecordError, Unit]]

  def searchBySecId(searchRequest: SearchHistoriesBySecIdRequest): F[List[HistoryRecord]]
}
object HistoryRepository {
  def apply[F[_]: Sync](implicit xa: Transactor[F]): HistoryRepository[F] = new HistoryRepository[F] {

    override def getHistoryRecord(id: HistoryRecordId): F[Option[HistoryRecord]] =
      sql"select $historyRecordFields from $historyRecordTableName where sec_id = ${id.securityId} and trade_date = ${id.tradeDate}"
        .query[HistoryRecord]
        .option
        .transact(xa)

    override def putHistoryRecord(historyRecord: HistoryRecord): F[Either[PutHistoryRecordError, Unit]] =
      (fr"insert into $historyRecordTableName ($historyRecordFields) " ++
        fr"values(${historyRecord.securityId}, ${historyRecord.tradeDate}, ${historyRecord.numTrades}, ${historyRecord.openPrice}, ${historyRecord.closePrice})").update.run
        .transact(xa)
        .map(_ => ())
        .attemptSomeSqlState {
          case sqlstate.class23.UNIQUE_VIOLATION      => HistoryRecordAlreadyExists
          case sqlstate.class23.FOREIGN_KEY_VIOLATION => SecurityNotFound
        }

    override def deleteHistoryRecord(id: HistoryRecordId): F[Either[DeleteHistoryRecordError, Unit]] =
      sql"delete from $historyRecordTableName where sec_id = ${id.securityId} and trade_date = ${id.tradeDate}".update.run
        .transact(xa)
        .map {
          case 0 => HistoryRecordDoesNotExist.asLeft[Unit]
          case _ => Right[DeleteHistoryRecordError, Unit](())
        }

    // Adding a PRIMARY KEY constraint will automatically create a unique btree index => Можно оптимально искать по префиксу ключа
    // В идеальной картине постгрес должен уметь в поиск и сортировку по частям композитного ключа
    override def searchBySecId(searchRequest: SearchHistoriesBySecIdRequest): F[List[HistoryRecord]] =
      (fr"select $historyRecordFields from $historyRecordTableName where sec_id = ${searchRequest.securityId} " ++
        fr"order by trade_date desc " ++
        fr"limit ${searchRequest.paginationOptions.flatMap(_.limit).getOrElse(defaultLimit)}" ++
        fr"offset ${searchRequest.paginationOptions.flatMap(_.offset).getOrElse(defaultOffset)}")
        .query[HistoryRecord]
        .to[List]
        .transact(xa)

    private val historyRecordTableName = fr"history_records"
    private val historyRecordFields = fr"sec_id, trade_date, num_trades, open_price, close_price"
    private val defaultLimit: NonNegativeInt = Refined.unsafeApply(1000)
    private val defaultOffset: NonNegativeInt = Refined.unsafeApply(0)
    // При рефакторинге неожиданно обнаружил что Refined неполноценно работает на третьей скале
  }
}
