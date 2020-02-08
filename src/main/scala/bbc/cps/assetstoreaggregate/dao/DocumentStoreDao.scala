package bbc.cps.assetstoreaggregate.dao

import bbc.cps.assetstoreaggregate.Config.Mongo
import bbc.cps.assetstoreaggregate.model.AssetDocument
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.jackson.Serialization.write
import org.mongodb.scala._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.ReplaceOptions
import scala.concurrent.Future

trait DocumentStoreDao extends JsonFormats {
  def find(filter: Bson): Future[Seq[Document]] = {
      Mongo.assetCollection.find(filter).toFuture()
  }

  def upsertAsset(assetDocument: AssetDocument): Future[result.UpdateResult] = {
    val filter = equal("assetId", assetDocument.assetId)
    val document = Document(write(assetDocument))
    Mongo.assetCollection.replaceOne(filter, document, ReplaceOptions().upsert(true)).head()
  }

  def create(document: Document): Future[Completed] = {
    Mongo.assetCollection.insertOne(document).toFuture()
  }
}

object DocumentStoreDao extends DocumentStoreDao
