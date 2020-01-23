package bbc.cps.assetstoreaggregate.dao

import bbc.cps.assetstoreaggregate.model.AssetDocument
import org.mongodb.scala._

import scala.concurrent.Future

trait DocumentStoreDao {
  val mongoClient: MongoClient = MongoClient()
  def upsertAsset(assetDocument: AssetDocument): Future[Unit] = {
    Future.successful(Unit)
  }
}

object DocumentStoreDao extends DocumentStoreDao
