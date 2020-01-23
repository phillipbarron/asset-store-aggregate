package bbc.cps.assetstoreaggregate

import akka.kafka.ConsumerSettings
import bbc.camscalatrachassis.config.ConfigChassis
import bbc.camscalatrachassis.config.ConfigChassis._
import bbc.cps.assetstoreaggregate.KafkaActorSystem.system
import bbc.cps.assetstoreaggregate.util.{AmazonS3ClientDummy, AmazonSNSClientDummy}
import bbc.cps.whitelist.{Whitelist, WhitelistFactory}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

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

  object S3Client {
    val historyS3Client: AmazonS3 = environment match {
      case DEV if System.getProperty("useRealS3Client", "false").equalsIgnoreCase("true") =>
        log.info(s">>> Using real S3 client on $environment")
        AmazonS3ClientBuilder.standard().build()
      case DEV | MGMT =>
        log.info(s">>> Using dummy S3 Client")
        AmazonS3ClientDummy
      case _ =>
        log.info(s">>> Using real S3 client on $environment")
        AmazonS3ClientBuilder.standard().build()
    }
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

  object Postgres {
    log.info("Creating PostgreSql client object.")

    private val defaultPostgresMaxConnections = "10"

    // FORMAT: "jdbc:postgresql://host:port/database|username|password"
    private val postgresConnectionUri = environment match {
      case DEV | MGMT => "jdbc:postgresql://localhost:5432/postgres|postgres|postgres"
      case _ => getSecureConfigurationAsString("postgres_connection_uri")
    }

    private val postgresMaxConnections = environment match {
      case DEV | MGMT => defaultPostgresMaxConnections.toInt
      case _ => getConfigValue("postgres_maxConnections").getOrElse(defaultPostgresMaxConnections).toInt
    }

    private val connectionUrlRegex = """^(.*)\|(.*)\|(.*)$""".r

    private val (url, username, password) = postgresConnectionUri match {
      case connectionUrlRegex(a, b, c) => (a, b, c)
      case _ => throw new MissingConfigurationException(s"Invalid postgres connection url: $postgresConnectionUri")
    }

    log.info(s">>> PostgreSql url: $url")
    log.info(s">>> PostgreSql username: $username")

    private val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName("org.postgresql.Driver")
    hikariConfig.setJdbcUrl(url)
    hikariConfig.setUsername(username)
    hikariConfig.setPassword(password)
    hikariConfig.setMaximumPoolSize(postgresMaxConnections)
    hikariConfig.setMinimumIdle(5)
    hikariConfig.setIdleTimeout(60000)
    hikariConfig.setAutoCommit(true)

    private val hikariDataSource = new HikariDataSource(hikariConfig)

    val db: PostgresProfile.backend.DatabaseDef = Database.forDataSource(hikariDataSource, None)

    def closeDbConnection() {
      log.info("Closing Postgres connection pool")
      hikariDataSource.close()
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

  val snapshotBucket: String = environment match {
    case LIVE => "cps-article-history-live"
    case _ => "cps-article-history"
  }
  log.info(s">>> Using Snapshot bucket name: $snapshotBucket")
}

class MissingConfigurationException(message: String) extends Exception(message)
