package bbc.cps.assetstoreaggregate

import java.util.concurrent.TimeUnit

import akka.kafka.ConsumerSettings
import bbc.camscalatrachassis.config.ConfigChassis
import bbc.camscalatrachassis.config.ConfigChassis._
import bbc.cps.assetstoreaggregate.KafkaActorSystem.system
import bbc.cps.assetstoreaggregate.dao.MongoClientFactory
import bbc.cps.assetstoreaggregate.util.AmazonSNSClientDummy
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}
import com.mongodb.WriteConcern
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.mongodb.scala.{Document, MongoCollection}
import org.slf4j.LoggerFactory


object Config {
  val applicationName: String = "asset-store-aggregate"

  val ConfigChassis(
  environment
  ) = initConfig

  private val log = LoggerFactory getLogger getClass

  def getSecureConfigurationAsString(key: String): String = getSecureConfigValue(key) match {
    case Some(value) => value
    case _ => throw new MissingConfigurationException(s"Could not find secure config for key [$key]")
  }

  def getConfigurationAsString(key: String): String = {
    getConfigValue(key) match {
      case Some(value) => value
      case None => throw new MissingConfigurationException(s"Could not find config for key [$key]")
    }
  }

  object Mongo {
    private val cosmosConfigUri = getSecureConfigValue("mongodb_connection_uri").getOrElse("")

    private val mongoConnectionUri = environment match {
      case INT | TEST | LIVE => cosmosConfigUri
      case _ => "mongodb://localhost"
    }

    private val mongoClient = MongoClientFactory.getClient(mongoConnectionUri)

    private val assetsDatabaseName = "assets-db"

    private val assetsDatabase = mongoClient.getDatabase(assetsDatabaseName)

    val assetCollection: MongoCollection[Document] = assetsDatabase.getCollection("assets")
      .withWriteConcern(WriteConcern.W1.withWTimeout(5, TimeUnit.SECONDS))

    log.info(s">>> Mongo connection URI: $mongoConnectionUri")
    log.info(s">>> Mongo database: $assetsDatabaseName")
  }

  object SNS {
    val historyNotificationsTopic: String = environment match {
      case DEV | MGMT => "historyNotificationsTopic"
      case _ => getConfigurationAsString("historyNotificationsTopic")
    }
    val historySNSClient: AmazonSNS = environment match {
      case DEV | MGMT =>
        log.info(s">>> Using dummy SNS Client")
        AmazonSNSClientDummy
      case _ =>
        log.info(s">>> Using real SNS client on $environment")
        AmazonSNSClientBuilder.standard()
          .withClientConfiguration(new ClientConfiguration())
          .withCredentials(InstanceProfileCredentialsProvider.getInstance())
          .withRegion(Regions.EU_WEST_1)
          .build()
    }
  }

  object MessagingClient {
    val DEEL_VERSION = "2.0.0"

    private val kafkaBootstrapServers = environment match {
      case DEV | MGMT => "localhost:9999"
      case _ => getConfigurationAsString("kafkaBootstrapServers")
    }

    val kafkaTopic: String = environment match {
      case DEV | MGMT => "testKafkaTopic"
      case _ => getConfigurationAsString("kafkaTopic")
    }

    val kafkaConsumerGroupId: String = environment match {
      case DEV | MGMT => "asset-store-aggregate"
      case _ => getConfigurationAsString("kafkaConsumerGroupId")
    }

    val consumerSettings: ConsumerSettings[String, String] = {
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(kafkaBootstrapServers)
        .withGroupId(kafkaConsumerGroupId)
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }
  }

}

class MissingConfigurationException(message: String) extends Exception(message)
