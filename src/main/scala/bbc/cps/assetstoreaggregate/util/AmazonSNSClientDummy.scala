package bbc.cps.assetstoreaggregate.util

import com.amazonaws.services.sns.AbstractAmazonSNS
import com.amazonaws.services.sns.model._
import org.json4s.JValue
import org.json4s.native.JsonMethods._


object AmazonSNSClientDummy extends AbstractAmazonSNS {

  val default: JValue = parse("{}")
  var latestNotification: JValue = default
  var allNotifications: Seq[JValue] = Seq.empty

  def reset() = synchronized {
    latestNotification = default
    allNotifications = Seq.empty
  }

  override def publish(arn: String, message: String): PublishResult = synchronized {
    val messageJson = parse(message)
    latestNotification = messageJson
    allNotifications +:= latestNotification

    new PublishResult().withMessageId((messageJson \ "stream").toString)
  }
}


