package bbc.cps.assetstoreaggregate.dao

import akka.Done
import akka.kafka.ConsumerMessage.{CommittableOffset, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import bbc.cps.assetstoreaggregate.KafkaActorSystem._
import org.slf4j.LoggerFactory
import scala.concurrent.Future

trait AkkaKafkaConsumer {
  private val log = LoggerFactory getLogger getClass
  private def commitKafkaOffset(source: Source[CommittableOffset, Consumer.Control]) =
    source
      .batch(max = 20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
        batch.updated(elem)
      }
      .mapAsync(100)(_.commitScaladsl())

  protected val consumerSettings: ConsumerSettings[String, String]
  protected val topic: KafkaTopic

  protected lazy val consumer: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
    Consumer.committableSource(consumerSettings, Subscriptions.topics(topic.name))

  protected def consumeStream(): Source[CommittableOffset, Consumer.Control]

  def consume(): Future[Done] = {
    log.info("Kafka consume has been called")
    commitKafkaOffset(consumeStream()).runWith(Sink.ignore)
  }
}

case class KafkaTopic(name: String)

