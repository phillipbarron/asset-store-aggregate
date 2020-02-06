package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.dao.DocumentStoreDao
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor.monitor
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.JsonAST.{JString, JValue}
import bbc.cps.assetstoreaggregate.model.Branch.{Published, Working}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.equal
import org.json4s.jackson.Serialization.{read, write}
import bbc.cps.assetstoreaggregate.model.EventType._
import bbc.cps.assetstoreaggregate.exceptions.AssetNotFoundException
import bbc.cps.assetstoreaggregate.model.Branch.Branch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AssetStoreService extends JsonFormats {
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
              setBranch(optimoEvent.data.payload.eventData, Working),
              existingDoc.publishedBranch
            )
          case _ =>
            AssetDocument(
              optimoEvent.data.payload.assetId,
              setBranch(optimoEvent.data.payload.eventData, Working),
              setBranch(optimoEvent.data.payload.eventData, Published)
            )
        }
      }
      case Nil => optimoEvent.data.payload.eventType match {
        case SAVED | CREATED | DELETED | RESTORED =>
          AssetDocument(
            optimoEvent.data.payload.assetId,
            setBranch(optimoEvent.data.payload.eventData, Working),
            JString("""{}""")
          )
        case _ =>
          println("there is no existing document and so we are returning a new one with both branches")
          AssetDocument(
            optimoEvent.data.payload.assetId,
            setBranch(optimoEvent.data.payload.eventData, Working),
            setBranch(optimoEvent.data.payload.eventData, Published)
          )
      }
    }
  }

  private def setBranch(asset: JValue, branch: Branch): JValue = {
    val bb = branch match {
      case Published => "published"
      case _ => "working"
    }
    val transformed = asset transformField {
      case ("branch", JString(_)) => ("branch", JString(bb))
    }
    println(s"we received the value $asset with the branch $branch and returning $transformed")
    transformed
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
