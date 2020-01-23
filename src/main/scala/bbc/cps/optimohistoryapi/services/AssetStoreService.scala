package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.dao.{DocumentStoreDao, EventsDao}
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor.monitor
import org.slf4j.LoggerFactory

import scala.concurrent.Future


trait AssetStoreService {
  private val log = LoggerFactory getLogger getClass
  protected val documentStoreDao: DocumentStoreDao

  private def optimoEventToAssetDocument(optimoEvent: OptimoEvent): AssetDocument = {
    AssetDocument(
      optimoEvent.data.payload.assetId,
      optimoEvent.data.payload.eventData,
      optimoEvent.data.payload.eventData
    )
  }

  def saveEvent(optimoEvent: OptimoEvent): Future[Unit] = monitor("save-document", "saveDocument") {
    val document = optimoEventToAssetDocument(optimoEvent)
    documentStoreDao.upsertAsset(document)
  }
}

object AssetStoreService extends AssetStoreService {
  protected val documentStoreDao: DocumentStoreDao = DocumentStoreDao
}
