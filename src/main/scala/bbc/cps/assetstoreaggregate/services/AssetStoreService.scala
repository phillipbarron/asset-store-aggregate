package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.dao.DocumentStoreDao
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor.monitor
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.JsonAST.{JString, JValue}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.equal
import org.slf4j.LoggerFactory
import org.json4s.jackson.Serialization.{read, write}
import bbc.cps.assetstoreaggregate.exceptions.BadRequestException
import bbc.cps.assetstoreaggregate.model.EventType._
import bbc.cps.assetstoreaggregate.exceptions.AssetNotFoundException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AssetStoreService extends JsonFormats {
  private val log = LoggerFactory getLogger getClass
  protected val documentStoreDao: DocumentStoreDao

  private def optimoEventToAssetDocument(optimoEvent: OptimoEvent): Future[AssetDocument] = {
    val filter = equal("assetId", optimoEvent.data.payload.assetId)
    documentStoreDao.find(filter) map {
      case Seq(document) => {
        val existingDoc = read[AssetDocument](document.toJson())
        optimoEvent.data.payload.eventType match {
          case SAVED | CREATED | DELETED | RESTORED =>
            AssetDocument(
              optimoEvent.data.payload.assetId,
              optimoEvent.data.payload.eventData,
              existingDoc.publishedBranch
            )
          case _ =>
            AssetDocument(
              optimoEvent.data.payload.assetId,
              optimoEvent.data.payload.eventData,
              optimoEvent.data.payload.eventData
            )
        }
      }
      case Nil => optimoEvent.data.payload.eventType match {
        case SAVED | CREATED | DELETED | RESTORED =>
          AssetDocument(
            optimoEvent.data.payload.assetId,
            optimoEvent.data.payload.eventData,
            JString("""{}""")
          )
        case _ =>
          AssetDocument(
            optimoEvent.data.payload.assetId,
            optimoEvent.data.payload.eventData,
            optimoEvent.data.payload.eventData
          )
      }
    }

  }

  def saveEvent(optimoEvent: OptimoEvent): Future[Unit] = monitor("save-document", "saveDocument") {
    optimoEventToAssetDocument(optimoEvent) map {
      document =>
        println("Saving event", document)
        documentStoreDao.create(Document(write(document)))
    }
  }

  def getAsset(assetId: String): Future[JValue] = {
    val filter = equal("assetId", assetId)
    documentStoreDao.find(filter) map {
      case Seq(document) => read[AssetDocument](document.toJson()).workingBranch
      case Nil => throw AssetNotFoundException("PANIC!")
    }
  }

  def getAssetBranch(assetId: String, branch: String): Future[JValue] = {
    val filter = equal("assetId", assetId)
    documentStoreDao.find(filter) map {
      case Seq(document) => {
        val assetDocument = read[AssetDocument](document.toJson())
        branch match {
          case "published" => assetDocument.publishedBranch
          case _ => assetDocument.workingBranch
        }
      }
      case Nil => throw AssetNotFoundException("PANIC!")
    }
  }
}


object AssetStoreService extends AssetStoreService {
  protected val documentStoreDao: DocumentStoreDao = DocumentStoreDao
}
