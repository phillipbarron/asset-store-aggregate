package util

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.collection.JavaConverters._
import bbc.cps.optimohistoryapi.Config.MessagingClient.kafkaTopic
import bbc.cps.optimohistoryapi.dao.OptimoKafkaConsumer

object KafkaUtil {
  private val kafkaHost = "localhost"
  private val kafkaPort = System.getenv("KAFKA_PORT")

  private val producerProperties = new Properties()
  producerProperties.put("bootstrap.servers", s"$kafkaHost:$kafkaPort")
  private val producer = new KafkaProducer[String, String](producerProperties, new StringSerializer(), new StringSerializer())

  private val consumerProperties = new Properties()
  consumerProperties.put("bootstrap.servers", s"$kafkaHost:$kafkaPort")
  consumerProperties.put("group.id", "test-asset-store-aggregate-consumer")
  consumerProperties.put("enable.auto.commit", "true")
  consumerProperties.put("auto.commit.interval.ms", "1000")

  private val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](consumerProperties, new StringDeserializer(), new StringDeserializer())

  OptimoKafkaConsumer.consume()
  @scala.annotation.tailrec
  private def waitForPartitionAssignment(): List[TopicPartition] = {
    consumer.assignment().asScala.toList match {
      case Nil =>
        println(">>> KafkaUtil: waiting for partition assignment")
        Thread.sleep(1000)
        // Need to call poll at least once to assign a partition to the consumer
        consumer.poll(Duration.ofMillis(100))
        waitForPartitionAssignment()
      case assignments => assignments
    }
  }

  println(s">>> KafkaUtil: Subscribing to topic $kafkaTopic")
  consumer.subscribe(List(kafkaTopic).asJava)
  waitForPartitionAssignment()

  def producerSend(message: String): Unit = {
    val record = new ProducerRecord[String, String](kafkaTopic, message)
    producer.send(record)
  }
}
