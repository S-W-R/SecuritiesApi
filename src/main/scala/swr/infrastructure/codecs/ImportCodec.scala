package swr.infrastructure.codecs

import java.time.LocalDate

import scala.util.Try
import scala.xml.Elem
import scala.xml.NodeSeq
import scala.xml.XML

import cats.syntax.all.catsSyntaxEq

import sttp.tapir.Codec
import sttp.tapir.Codec.XmlCodec
import sttp.tapir.DecodeResult
import sttp.tapir.Schema
import sttp.tapir.generic.auto.*

import swr.datamodels.history.HistoryRecord
import swr.datamodels.security.Security

object ImportCodec { // Кажется решение далеко от идеального, зато дешево и быстро решает множество проблем
  implicit val securitiesCodec: XmlCodec[List[Security]] =
    Codec.xml(raw => DecodeResult.Value(securitiesFromXml(XML.loadString(raw))))(_ => "")

  implicit val historyRecordsCodec: XmlCodec[List[HistoryRecord]] =
    Codec.xml(raw => DecodeResult.Value(historyRecordsFromXml(XML.loadString(raw))))(_ => "")

  private def securitiesFromXml(root: Elem): List[Security] = root
    .getRowsById("securities")
    .map(row => {
      val id = row \@ "id"
      val secId = row \@ "secid"
      val name = row \@ "name"
      val regNumber = row \@ "regnumber"
      val emitent = row \@ "emitent_title"
      Security(
        id = id.toInt, // todo ToIntoption, сейчас выдаст 400
        securityId = secId,
        name = name,
        registrationNumber = regNumber,
        emitentTitle = emitent
      )
    })
    .toList
  private def historyRecordsFromXml(root: Elem): List[HistoryRecord] = root
    .getRowsById("history")
    .map(row => {
      val secId = row \@ "SECID"
      val tradeDate = row \@ "TRADEDATE"
      val numTrades = row \@ "NUMTRADES"
      val openPrice = row \@ "OPEN"
      val closePrice = row \@ "CLOSE"
      HistoryRecord(
        securityId = secId,
        tradeDate = LocalDate.parse(tradeDate),
        numTrades = BigDecimal(numTrades),
        openPrice = Try(BigDecimal(openPrice)).toOption,
        closePrice = Try(BigDecimal(closePrice)).toOption
      )
    })
    .toList

  extension (e: Elem)
    private def getRowsById(id: String): NodeSeq = (e \ "data").filter(data => data \@ "id" === id) \ "rows" \ "row"

}
