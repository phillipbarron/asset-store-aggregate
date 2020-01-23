package bbc.cps.assetstoreaggregate.dao

import org.mongodb.scala.MongoClient

object MongoClientFactory {
  def getClient(connectionUri: String): MongoClient = {
    MongoClient(connectionUri)
  }
}
