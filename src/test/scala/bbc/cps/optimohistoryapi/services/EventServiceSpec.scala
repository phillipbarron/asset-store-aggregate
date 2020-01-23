package bbc.cps.assetstoreaggregate.services

import java.time.Instant
import java.util.UUID

import bbc.camscalatrachassis.concurrent.CustomExecutionContext
import bbc.cps.assetstoreaggregate.dao.EventsDao
import bbc.cps.assetstoreaggregate.model.EventType.EventType
import bbc.cps.assetstoreaggregate.model._
import org.json4s.native.JsonMethods.parse
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, _}
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.{BeforeAndAfter, FlatSpec}
import util.AppTestUtil
import org.scalatest.MustMatchers._
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class EventServiceSpec extends FlatSpec with AppTestUtil with MockitoSugar with BeforeAndAfter {
  private val mockEventsDao = mock[EventsDao]

  private val assetId = "cq915kk4wy5o"
  private val eventId = UUID.fromString("dae6dab7-86da-4105-8825-afd3c2fe37a0")
  private val userId = "someuser@bbc.co.uk"
  private val eventCreated = Instant.ofEpochMilli(1564497090000L)

  private def event(eventType: EventType, majorVersion: Int, minorVersion: Int) = {
    Event(
      eventType,
      userId,
      eventCreated,
      eventId,
      assetId,
      majorVersion,
      minorVersion
    )
  }

  val payloadJson = """
  {
  "foo": "foo",
  "bar": "bar",
  "baz": "baz"
  }
  """

  private def getOptimoEvent(eventType: EventType) = {
    OptimoEvent(
      Data(
        Payload(
          eventId,
          eventType,
          eventCreated,
          assetId,
          userId,
          parse(payloadJson)
        )
      )
    )
  }

  before {
    reset(mockEventsDao)
  }

  object TestEventService extends EventService {
    protected val eventsDao: EventsDao = mockEventsDao
  }

  "saveEvent()" should "save event with minorVersion and majorVersion as 0 when the asset has had no previous events and event is not a `Published`" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(None))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))
    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.SAVED)), 10.seconds)
    verify(mockEventsDao).saveEvent(event(EventType.CREATED, majorVersion = 0, minorVersion = 0))
  }

  it should "save an event and not increment minorVersion or majorVersion when the event is a `save` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.SAVED)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.SAVED, majorVersion = 1, minorVersion = 0))
  }

  it should "save an event and only increment minorVersion, not majorVersion when the event is a `PublishedMinorChange` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.PUBLISHED_MINOR_CHANGE)), 10.seconds)

    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.PUBLISHED_MINOR_CHANGE, majorVersion = 1, minorVersion = 1))
  }

  it should "save an event and set minorVersion and majorVersion to 0 when the event is a `Created` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.CREATED)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.CREATED, majorVersion = 1, minorVersion = 0))
  }

  it should "save an event and not increment minorVersion or majorVersion when the event is a `Deleted` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.DELETED)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.DELETED, majorVersion = 1, minorVersion = 0))
  }

  it should "save an event and reset minorVersion to 0 and increment majorVersion when the event is a `Published` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 1))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.PUBLISHED)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.PUBLISHED, majorVersion = 2, minorVersion = 0))
  }

  it should "save an event and reset minorVersion to 0 and increment majorVersion when the event is a `PublishedNewsUpdate` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 1))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.PUBLISHED_NEWS_UPDATE)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.PUBLISHED_NEWS_UPDATE, majorVersion = 2, minorVersion = 0))
  }

  it should "save an asset with minorVersion 0 and majorVersion 1 when the event is `Published` and the asset has had no previous events" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(None))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.PUBLISHED)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))
  }

  it should "save an event and not increment minorVersion or majorVersion when the event is a `Withdrawn` event" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(Some(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.successful(1))

    Await.result(TestEventService.saveEvent(getOptimoEvent(EventType.WITHDRAWN)), 10.seconds)
    verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
    verify(mockEventsDao).saveEvent(event(EventType.WITHDRAWN, majorVersion = 1, minorVersion = 0))
  }

  it should "return a failed Future if saving the event fails" in {
    when(mockEventsDao.getMostRecentEvent(assetId, eventCreated)).thenReturn(Future.successful(None))
    when(mockEventsDao.saveEvent(any[Event])).thenReturn(Future.failed(new Exception("oops")))

    val result = TestEventService.saveEvent(getOptimoEvent(EventType.PUBLISHED))
    ScalaFutures.whenReady(result.failed) { e =>
      e mustBe a[Exception]
      e.getMessage mustBe "oops"

      verify(mockEventsDao).getMostRecentEvent(assetId, eventCreated)
      verify(mockEventsDao).saveEvent(event(EventType.PUBLISHED, majorVersion = 1, minorVersion = 0))
    }
  }

}
