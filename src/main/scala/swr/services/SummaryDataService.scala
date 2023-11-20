package swr.services

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all.*

import swr.datamodels.history.ExtendedHistoryRecord
import swr.datamodels.history.HistoryRecord
import swr.datamodels.history.HistoryRecordId
import swr.datamodels.history.requests.SearchHistoriesBySecIdRequest
import swr.datamodels.security.Security
import swr.datamodels.security.SecuritySummaryInfo
import swr.datamodels.security.requests.GetSecuritySummaryInfoRequest
import swr.repositories.HistoryRepository
import swr.repositories.SecuritiesRepository

trait SummaryDataService[F[_]: Sync] {
  def getSecuritySummaryInfo(request: GetSecuritySummaryInfoRequest): F[Option[SecuritySummaryInfo]]

  def getExtendedHistoryRecord(id: HistoryRecordId): F[Option[ExtendedHistoryRecord]]
}

object SummaryDataService {
  def apply[F[_]: Sync](implicit
      historyRepository: HistoryRepository[F],
      securitiesRepository: SecuritiesRepository[F]
  ): SummaryDataService[F] = new SummaryDataService[F] {

    override def getSecuritySummaryInfo(request: GetSecuritySummaryInfoRequest): F[Option[SecuritySummaryInfo]] = {
      securitiesRepository.getSecurityById(request.id) >>= {
        case Some(security: Security) =>
          historyRepository
            .searchBySecId(SearchHistoriesBySecIdRequest(security.securityId, request.paginationOptions))
            .map(history => SecuritySummaryInfo(security, history).some)
        case None => None.pure[F]
      }
    }

    override def getExtendedHistoryRecord(id: HistoryRecordId): F[Option[ExtendedHistoryRecord]] = {
      val extendedHistoryRecord = for {
        history <- OptionT[F, HistoryRecord](historyRepository.getHistoryRecord(id))
        security <- OptionT[F, Security](securitiesRepository.getSecurityBySecId(id.securityId))
      } yield ExtendedHistoryRecord(
        securityId = security.securityId,
        name = security.name,
        registrationNumber = security.registrationNumber,
        emitentTitle = security.emitentTitle,
        tradeDate = history.tradeDate,
        numTrades = history.numTrades,
        openPrice = history.openPrice,
        closePrice = history.closePrice
      )
      extendedHistoryRecord.value
    }
  }
}
