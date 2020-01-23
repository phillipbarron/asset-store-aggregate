package bbc.cps.assetstoreaggregate.services

import java.time.Instant
import java.util.UUID

import bbc.camscalatrachassis.concurrent.CustomExecutionContext
import bbc.cps.assetstoreaggregate.model.EventType.EventType
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.util.JsonFormats
import org.json4s.native.JsonMethods.parse
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, _}
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatestplus.mockito.MockitoSugar
import util.AppTestUtil

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class OptimoEventServiceSpec extends FlatSpec with AppTestUtil with MockitoSugar with BeforeAndAfter with JsonFormats {
  private val mockEventService = mock[EventService]
  private val mockNotificationService = mock[NotificationService]
  private val mockSnapshotService = mock[SnapshotService]
  private val payloadJson = """
  {
  "foo": "foo",
  "bar": "bar",
  "baz": "baz"
  }
  """

  private val assetId = "cq915kk4wy5o"
  private val entityId = s"urn:bbc:optimo:asset:$assetId"
  private val eventId = UUID.fromString("dae6dab7-86da-4105-8825-afd3c2fe37a0")
  private val userId = "someuser@bbc.co.uk"
  private val eventCreated = Instant.ofEpochMilli(1564497090000L)

  private def getOptimoEvent(eventType: EventType) = {
    OptimoEvent(
      Data(
        Payload(
          eventId,
          eventType,
          eventCreated,
          entityId,
          userId,
          parse(payloadJson)
        )
      )
    )
  }
  before {
    reset(mockEventService, mockSnapshotService, mockNotificationService)
  }

  object TestOptimoEventService extends OptimoEventService {
    override protected val assetStoreService: EventService = mockEventService
    override protected val snapshotService: SnapshotService = mockSnapshotService
    override protected val notificationService: NotificationService = mockNotificationService
  }

  "processEvent()" should "write the event, the snapshot and send a notification" in {
    val testAssetOptimo = getOptimoEvent(EventType.PUBLISHED)
    when(mockEventService.saveEvent(any[OptimoEvent])).thenReturn(Future.successful(()))
    when(mockSnapshotService.saveSnapshot(any[OptimoEvent])).thenReturn(Future.successful(()))

    Await.result(TestOptimoEventService.processEvent(testAssetOptimo), 10.seconds)
    verify(mockEventService).saveEvent(testAssetOptimo)
    verify(mockSnapshotService).saveSnapshot(testAssetOptimo)
    verify(mockNotificationService).sendHistoryChangeNotification(assetId)
  }

  it should "if writing the event fails still write the snapshot but don't send a notification" in {
    val testOptimoEvent = getOptimoEvent(EventType.PUBLISHED)
    when(mockEventService.saveEvent(any[OptimoEvent])).thenReturn(Future.failed(new Exception("oops")))
    when(mockSnapshotService.saveSnapshot(any[OptimoEvent])).thenReturn(Future.successful(()))

    Await.result(TestOptimoEventService.processEvent(testOptimoEvent), 10.seconds)
    verify(mockEventService).saveEvent(testOptimoEvent)
    verify(mockSnapshotService).saveSnapshot(testOptimoEvent)
    verifyZeroInteractions(mockNotificationService)
  }

  it should "if writing the snapshot fails still write the event and send a notification" in {
    val testOptimoEvent = getOptimoEvent(EventType.PUBLISHED)
    when(mockEventService.saveEvent(any[OptimoEvent])).thenReturn(Future.successful(()))
    when(mockSnapshotService.saveSnapshot(any[OptimoEvent])).thenReturn(Future.failed(new Exception("oops")))

    Await.result(TestOptimoEventService.processEvent(testOptimoEvent), 10.seconds)
    verify(mockEventService).saveEvent(testOptimoEvent)
    verify(mockSnapshotService).saveSnapshot(testOptimoEvent)
    verify(mockNotificationService).sendHistoryChangeNotification(assetId)
  }

  it should "returns successfully if both writing event and snapshot fail" in {
    val testOptimoEvent = getOptimoEvent(EventType.PUBLISHED)
    when(mockEventService.saveEvent(any[OptimoEvent])).thenReturn(Future.failed(new Exception("oops 1")))
    when(mockSnapshotService.saveSnapshot(any[OptimoEvent])).thenReturn(Future.failed(new Exception("oops 2")))

    Await.result(TestOptimoEventService.processEvent(testOptimoEvent), 10.seconds)
    verify(mockEventService).saveEvent(testOptimoEvent)
    verify(mockSnapshotService).saveSnapshot(testOptimoEvent)
    verifyZeroInteractions(mockNotificationService)
  }
}
