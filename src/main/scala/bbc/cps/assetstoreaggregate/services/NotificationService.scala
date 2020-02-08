package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.Config
import bbc.cps.assetstoreaggregate.model.Notification
import bbc.cps.assetstoreaggregate.util.JsonFormats
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishResult
import org.json4s.native.Serialization._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

trait NotificationService extends JsonFormats {
  protected val snsClient: AmazonSNS
  private val log = LoggerFactory getLogger getClass

  def sendHistoryChangeNotification(assetId: String): Try[PublishResult] = {
    val notification = Notification("history", assetId)
    Try(snsClient.publish(Config.SNS.historyNotificationsTopic, write(notification))) match {
      case result@Success(publishResult) =>
        log.info(s"Successfully sent history change notification for asset [$assetId] with message ID: [${publishResult.getMessageId}]")
        result
      case result@Failure(e) =>
        log.error(s"Failed to send history change notification for asset [$assetId] with error: [${e.getMessage}]")
        result
    }
  }
}

object NotificationService extends NotificationService {
  override protected val snsClient: AmazonSNS = Config.SNS.historySNSClient
}
