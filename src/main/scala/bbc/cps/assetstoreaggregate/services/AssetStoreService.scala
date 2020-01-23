package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.dao.DocumentStoreDao
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor.monitor
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.JsonAST.JValue
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import org.json4s.jackson.Serialization.write

trait AssetStoreService extends JsonFormats {
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
    println("WE ARE SAVING THE EVENT!!!!", document)
    documentStoreDao.create(Document(write(document)))
  }

  def getAsset(assetId: String): Future[JValue] = {
    documentStoreDao.find(eq("assetId", assetId))
  }
}

object AssetStoreService extends AssetStoreService {
  protected val documentStoreDao: DocumentStoreDao = DocumentStoreDao
}
