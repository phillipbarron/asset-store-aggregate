package bbc.cps.assetstoreaggregate.dao

import bbc.cps.assetstoreaggregate.model.AssetDocument

import scala.concurrent.Future

trait DocumentStoreDao {

  def upsertAsset(assetDocument: AssetDocument): Future[Unit] = {
    Future.successful(Unit)
  }
}

object DocumentStoreDao extends DocumentStoreDao
