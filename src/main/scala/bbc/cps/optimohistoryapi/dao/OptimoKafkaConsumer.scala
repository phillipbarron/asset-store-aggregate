package bbc.cps.assetstoreaggregate.dao

import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings}
import akka.stream.scaladsl.Source
import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.cps.assetstoreaggregate.Config.MessagingClient.{kafkaTopic, consumerSettings => kafkaConsumerSettings}
import bbc.cps.assetstoreaggregate.model.OptimoEvent
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor._
import bbc.cps.assetstoreaggregate.services.OptimoEventService
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.native.Serialization.read
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait OptimoKafkaConsumer extends AkkaKafkaConsumer with JsonFormats {

  protected val topic: KafkaTopic = KafkaTopic(kafkaTopic)
  protected val consumerSettings: ConsumerSettings[String, String] = kafkaConsumerSettings
  private val log = LoggerFactory getLogger getClass

  private def toOptimoEvent(message: ConsumerMessage.CommittableMessage[String, String]): (CommittableOffset, OptimoEvent) = {
    Try {
      val optimoEvent = read[OptimoEvent](message.record.value)
      message.committableOffset -> optimoEvent
    } match {
      case Success(result) => result
      case Failure(e) =>
        log.error(s"Error consuming event ${message.record.value}", e)
        incrementMetric("errors-consuming-event", "errors.consumingEvent")
        throw new Exception(s">>> ConsumerService ERROR!! $e")
    }
  }

  private def logOutput: PartialFunction[(CommittableOffset, OptimoEvent), (CommittableOffset, OptimoEvent)] = {
    case (offset, optimoEvent) =>
      log.info(s"consuming domain event: $optimoEvent")
      incrementMetric("event-consumed", "event.consumed")
      offset -> optimoEvent
  }

  private def process(streamProcessor: OptimoEvent => Future[Unit]): PartialFunction[(CommittableOffset, OptimoEvent), Future[CommittableOffset]] = {
      case (offset, assetEvent) =>
      monitor("process-stream", "processStream") {
        streamProcessor(assetEvent) map { _ => offset }
      }
  }

  protected def consumeStream(): Source[CommittableOffset, Consumer.Control] = {
    consumer
      .map(toOptimoEvent)
      .map(logOutput)
      .mapAsync(1)(process(OptimoEventService.processEvent))
  }
}

object OptimoKafkaConsumer extends OptimoKafkaConsumer
