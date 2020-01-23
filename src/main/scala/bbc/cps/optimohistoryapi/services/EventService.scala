package bbc.cps.assetstoreaggregate.services

import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.cps.assetstoreaggregate.dao.EventsDao
import bbc.cps.assetstoreaggregate.model._
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor._

import scala.concurrent.Future

trait EventService {
  protected val eventsDao: EventsDao

  private def optimoEventToEventMapper(assetEvent: OptimoEvent): Event = {
    Event(
      assetEvent.data.payload.eventType,
      assetEvent.data.payload.userId,
      assetEvent.data.payload.eventTimestamp,
      assetEvent.data.payload.eventId,
      assetEvent.data.payload.assetId,
      0,
      0
    )
  }

  private def versionedEvent(event: Event, mostRecentEvent: Option[Event]) = {
    val currentMajorVersion = mostRecentEvent.map(_.majorVersion).getOrElse(0)
    val currentMinorVersion = mostRecentEvent.map(_.minorVersion).getOrElse(0)
    (event.eventType, mostRecentEvent) match {
      case (EventType.SAVED, None) =>
        event.copy(eventType = EventType.CREATED)
      case (EventType.PUBLISHED_MINOR_CHANGE, _) =>
        event.copy(majorVersion = currentMajorVersion, minorVersion = currentMinorVersion + 1)
      case (EventType.PUBLISHED | EventType.PUBLISHED_NEWS_UPDATE, _) =>
        event.copy(majorVersion = currentMajorVersion + 1, minorVersion = 0)
      case _ =>
        event.copy(majorVersion = currentMajorVersion, minorVersion = currentMinorVersion)
    }
  }

  def saveEvent(optimoEvent: OptimoEvent): Future[Unit] = monitor("save-event", "saveEvent") {
    val event = optimoEventToEventMapper(optimoEvent)
    for {
      mostRecentEvent <- eventsDao.getMostRecentEvent(event.assetId, event.eventCreated)
      eventToSave = versionedEvent(event, mostRecentEvent)
      _ <- eventsDao.saveEvent(eventToSave)
    } yield {}
  }
}

object EventService extends EventService {
  override protected val eventsDao: EventsDao = EventsDao
}
