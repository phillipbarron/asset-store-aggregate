package bbc.cps.optimohistoryapi.services

import bbc.cps.optimohistoryapi.model.Notification
import bbc.cps.optimohistoryapi.util.JsonFormats
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishResult
import org.json4s.native.Serialization._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.MustMatchers._
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatestplus.mockito.MockitoSugar
import util.AppTestUtil

import scala.util.Failure


class NotificationServiceSpec extends FlatSpec with AppTestUtil with MockitoSugar with BeforeAndAfter with JsonFormats {

  private val mockSnsClient = mock[AmazonSNS]

  before {
    reset(mockSnsClient)
  }

  object TestNotificationService extends NotificationService {
    override val snsClient: AmazonSNS = mockSnsClient
  }

  "sendHistoryChangeNotification()" should "send an SNS notification containing notification object" in {
    val publishResult = new PublishResult().withMessageId("test-message-id")
    val notification = Notification("history", "foo")

    when(mockSnsClient.publish(any[String], any[String])) thenReturn publishResult
    TestNotificationService.sendHistoryChangeNotification("foo")

    val value = ArgumentCaptor.forClass(classOf[String])

    verify(mockSnsClient).publish(any[String], value.capture())
    value.getValue mustBe write(notification)
  }

  "sendHistoryChangeNotification()" should "return failure where publish fails" in {
    val exception = new RuntimeException("Error sending notification")

    when(mockSnsClient.publish(any[String], any[String])) thenThrow exception
    val sendNotificationResult = TestNotificationService.sendHistoryChangeNotification("foo")

    sendNotificationResult mustBe Failure(exception)
  }

}
