package swr.services

import cats.effect.Sync
import cats.syntax.all.*

import org.typelevel.log4cats.Logger

import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.HistoryRecordId
import swr.datamodels.history.requests.SearchHistoriesBySecIdRequest
import swr.datamodels.history.responses.DeleteHistoryRecordResponses.DeleteHistoryRecordError
import swr.datamodels.history.responses.ImportHistoryRecordsResponses.ImportHistoryRecordsResponse
import swr.datamodels.history.responses.PutHistoryRecordResponses.PutHistoryRecordError
import swr.repositories.HistoryRepository

trait HistoryService[F[_]: Sync] {
  def getHistoryRecord(id: HistoryRecordId): F[Option[HistoryRecord]]

  def putHistoryRecord(historyRecord: HistoryRecord): F[Either[PutHistoryRecordError, Unit]]

  def deleteHistoryRecord(id: HistoryRecordId): F[Either[DeleteHistoryRecordError, Unit]]

  def searchHistoriesBySecId(searchRequest: SearchHistoriesBySecIdRequest): F[List[HistoryRecord]]

  def importHistoryRecords(historyRecords: List[HistoryRecord]): F[ImportHistoryRecordsResponse]
}

object HistoryService {
  def apply[F[_]: Sync: Logger](implicit historyRepository: HistoryRepository[F]): HistoryService[F] =
    new HistoryService[F] {
      override def getHistoryRecord(id: HistoryRecordId): F[Option[HistoryRecord]] =
        historyRepository.getHistoryRecord(id)

      override def putHistoryRecord(historyRecord: HistoryRecord): F[Either[PutHistoryRecordError, Unit]] =
        historyRepository.putHistoryRecord(historyRecord)

      override def deleteHistoryRecord(id: HistoryRecordId): F[Either[DeleteHistoryRecordError, Unit]] =
        historyRepository.deleteHistoryRecord(id)

      override def searchHistoriesBySecId(searchRequest: SearchHistoriesBySecIdRequest): F[List[HistoryRecord]] =
        historyRepository.searchBySecId(searchRequest)

      override def importHistoryRecords(historyRecords: List[HistoryRecord]): F[ImportHistoryRecordsResponse] =
        historyRecords
          .traverse(x =>
            putHistoryRecord(x).map { res =>
              {
                val id = HistoryRecordId(x.securityId, x.tradeDate)
                res match {
                  case _ @Left(value)  => id.asLeft
                  case _ @Right(value) => id.asRight
                }
              }
            }
          )
          .map(_.partitionMap(identity).swap)
          .map(ImportHistoryRecordsResponse.apply)
    }
}
