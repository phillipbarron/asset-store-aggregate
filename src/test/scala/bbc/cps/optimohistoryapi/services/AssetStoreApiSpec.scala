package bbc.cps.assetstoreaggregate.services

import java.time.Instant
import java.util.UUID

import bbc.camscalatrachassis.concurrent.CustomExecutionContext
import bbc.cps.assetstoreaggregate.api.RequestParams
import bbc.cps.assetstoreaggregate.dao.EventsDao
import bbc.cps.assetstoreaggregate.exceptions.{DatabaseException, HistoryNotFoundException}
import bbc.cps.assetstoreaggregate.model._
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, _}
import org.scalatest.MustMatchers._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatestplus.mockito.MockitoSugar
import util.AppTestUtil

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class AssetStoreApiSpec extends FlatSpec with AppTestUtil with MockitoSugar with BeforeAndAfter {
  private val mockEventsDao = mock[EventsDao]

  private val assetId = "cq915kk4wy5o"

  before {
    reset(mockEventsDao)
  }

  object TestHistoryService extends AssetStoreApi {
    protected val eventsDao: EventsDao = mockEventsDao
  }

  val event1 = Event(
    EventType.SAVED,
    "someuser@bbc.co.uk",
    Instant.ofEpochMilli(1564497090000L),
    UUID.fromString("dae6dab7-86da-4105-8825-afd3c2fe37a0"),
    assetId,
    0,
    0
  )

  val event2 = Event(
    EventType.PUBLISHED,
    "someuser@bbc.co.uk",
    Instant.ofEpochMilli(1564499090000L),
    UUID.fromString("dae6dab7-86da-4105-8825-afd3c2fe37b1"),
    assetId,
    1,
    0
  )

  val event3 = Event(
    EventType.PUBLISHED_MINOR_CHANGE,
    "someuser@bbc.co.uk",
    Instant.ofEpochMilli(1574499090000L),
    UUID.fromString("dae6dab7-86da-4105-8825-afd3c2fe37c2"),
    assetId,
    1,
    1
  )

  "getHistory()" should "return the history for an asset given its ID" in {
    when(mockEventsDao.getEvents(any[String], any[Int], any[Int])).thenReturn(Future.successful(List(event3, event2, event1),3))

    val requestParams = RequestParams(assetId, 1, 10)
    val history = Await.result(TestHistoryService.getHistory(requestParams), 10.seconds)

    val expectedPagination = PaginationInfo(None)
    val expectedHistory = AssetHistory(
      assetId,
      List(VersionHistory(
        event2.majorVersion,
        List(
          HistoryEvent(
            event3.eventId,
            event3.eventType,
            new DateTime(1574499090000L, DateTimeZone.UTC),
            event3.userId,
            event3.minorVersion
          ),
          HistoryEvent(
            event2.eventId,
            event2.eventType,
            new DateTime(1564499090000L, DateTimeZone.UTC),
            event2.userId,
            event2.minorVersion
          )
        )
      ),
        VersionHistory(
        event1.majorVersion,
          List(HistoryEvent(
            event1.eventId,
            event1.eventType,
            new DateTime(1564497090000L, DateTimeZone.UTC),
            event1.userId,
            event1.minorVersion
          ))
      )),
      expectedPagination)

    history mustBe expectedHistory
  }

  it should "pass query offset and size to the EventsDao given page and size params" in {
    when(mockEventsDao.getEvents(any[String], any[Int], any[Int])).thenReturn(Future.successful(List(event1, event2, event3), 3))

    val requestParams = RequestParams(assetId, 2, 5)
    Await.result(TestHistoryService.getHistory(requestParams), 10.seconds)

    verify(mockEventsDao).getEvents(assetId, 5, 5)
  }

  it should "return a failed future when retrieving events fails" in {
    when(mockEventsDao.getEvents(any[String], any[Int], any[Int])).thenReturn(Future.failed(new Exception("oops")))

    val requestParams = RequestParams(assetId, 1 , 1)

    ScalaFutures.whenReady(TestHistoryService.getHistory(requestParams).failed) { error =>
      error mustBe a[DatabaseException]
      error.getMessage mustBe s"An error occurred when retrieving history for asset $assetId"
    }
  }

  it should "return a failed future when there are no events for an asset" in {
    when(mockEventsDao.getEvents(any[String], any[Int], any[Int])).thenReturn(Future.successful(List(), 0))

    val requestParams = RequestParams(assetId, 1 , 1)

    ScalaFutures.whenReady(TestHistoryService.getHistory(requestParams).failed) { error =>
      error mustBe a[HistoryNotFoundException]
      error.getMessage mustBe s"History for asset [$assetId] at page [1] with size [1] does not exist"
    }
  }
}
