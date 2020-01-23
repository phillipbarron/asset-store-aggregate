package bbc.cps.optimohistoryapi.services

import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.cps.optimohistoryapi.api.RequestParams
import bbc.cps.optimohistoryapi.dao.EventsDao
import bbc.cps.optimohistoryapi.exceptions.{DatabaseException, HistoryNotFoundException}
import bbc.cps.optimohistoryapi.model._
import bbc.cps.optimohistoryapi.monitoring.HistoryApiMonitor
import bbc.cps.optimohistoryapi.monitoring.HistoryApiMonitor._
import bbc.cps.optimohistoryapi.util.PaginationPageValues
import org.joda.time.{DateTime, DateTimeZone}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

trait HistoryService {
  private val log = LoggerFactory getLogger getClass
  protected val eventsDao: EventsDao
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

  private def toHistoryEvent: PartialFunction[Event, HistoryEvent] = {
    case Event(eventType, userId, eventCreated, eventId, _, _, minorVersion) =>
      val timestamp = new DateTime(eventCreated.toEpochMilli, DateTimeZone.UTC)
      HistoryEvent(eventId, eventType, timestamp, userId, minorVersion)
  }

  private def majorVersion: PartialFunction[(Int, List[HistoryEvent]), Int] = {
    case (majorVersion, _) => majorVersion
  }

  private def createVersions( events: List[Event] )= {
    events
      .groupBy(_.majorVersion)
      .mapValues(_.map(toHistoryEvent))
      .toList.sortBy(majorVersion).reverse
      .map(VersionHistory.tupled)
  }

  private def getEvents(params: RequestParams) = {
    val offset = PaginationPageValues.offset(params.page, params.pageSize)

    monitor("dao-events-getEvents", "dao.events.getEvents") {
      eventsDao.getEvents(params.assetId, offset, params.pageSize) recoverWith {
        case e =>
          val errorMessage = s"An error occurred when retrieving history for asset ${params.assetId}"
          log.error(errorMessage, e)
          HistoryApiMonitor.increment("dao.events.errors")
          Future.failed(DatabaseException(errorMessage))
      }
    }
  }

 private def createPagination(requestParams: RequestParams, totalResults: Int) = {
    val nextPage = PaginationPageValues.nextPage(requestParams.pageSize, requestParams.page, totalResults)
    PaginationInfo(nextPage)
  }

  def getHistory(params: RequestParams): Future[AssetHistory] =
    getEvents(params) flatMap {
      case (Nil, 0) => Future.failed(HistoryNotFoundException(s"History for asset [${params.assetId}] at page [${params.page}] with size [${params.pageSize}] does not exist"))
      case (events, total) =>
        val versions = createVersions(events)
        val pagination= createPagination(params, total)
        Future.successful(AssetHistory(params.assetId, versions, pagination))
    }
}

object HistoryService extends HistoryService {
  protected val eventsDao: EventsDao = EventsDao
}

object PaginationPageValues extends PaginationPageValues