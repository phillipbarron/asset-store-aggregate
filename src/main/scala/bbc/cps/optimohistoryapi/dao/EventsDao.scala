package bbc.cps.optimohistoryapi.dao

import java.time.Instant

import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.cps.optimohistoryapi.Config
import bbc.cps.optimohistoryapi.Config.Postgres
import bbc.cps.optimohistoryapi.model.{Event, EventsTable}
import bbc.cps.optimohistoryapi.util.SlickTypeExtensionProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

trait EventsDao {
  private val events = TableQuery[EventsTable]
  implicit def applicationName = Config.applicationName
  implicit def environment = Config.environment

  def getEvents(assetId: String, offset: Int, size: Int): Future[(List[Event], Int)] = Postgres.db.run {
    for {
      numberOfEvents <- events.filter(_.assetId === assetId).length.result
      pagedEvents <- events
        .filter(_.assetId === assetId)
        .sortBy(_.eventCreated.desc)
        .drop(offset)
        .take(size)
        .result
        .map(_.toList)
    } yield(pagedEvents, numberOfEvents)
  }

  def saveEvent(event: Event): Future[Int] = {
    Postgres.db.run(events += event)
  }

  def getMostRecentEvent(assetId: String, eventCreated: Instant): Future[Option[Event]] = {
    Postgres.db.run(events
      .filter( event => event.assetId === assetId && event.eventCreated <= eventCreated)
      .sortBy(_.eventCreated.desc)
      .result
      .headOption
    )
  }
}

object EventsDao extends EventsDao