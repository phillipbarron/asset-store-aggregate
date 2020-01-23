package bbc.cps.assetstoreaggregate.dao

import bbc.cps.assetstoreaggregate.Config.Mongo
import bbc.cps.assetstoreaggregate.model.AssetDocument
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson

import scala.concurrent.Future

trait DocumentStoreDao {
  def find(filter: Bson): Future[Seq[Document]] = {
      Mongo.assetCollection.find(filter).toFuture()
  }

  def upsertAsset(assetDocument: AssetDocument): Future[Unit] = {
    Future.successful(Unit)
  }

  def create(document: Document): Future[Unit] = {
    Mongo.assetCollection.insertOne(document).toFuture()
    Future.successful(Unit)
  }
}

object DocumentStoreDao extends DocumentStoreDao
