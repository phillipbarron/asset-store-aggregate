package bbc.cps.assetstoreaggregate.model

import java.time.Instant
import java.util.UUID

import bbc.cps.assetstoreaggregate.model.EventType.EventType
import bbc.cps.assetstoreaggregate.util.SlickTypeExtensionProfile.api._
import slick.sql.SqlProfile.ColumnOption.NotNull

class EventsTable(tag: Tag) extends Table[Event](tag, "events") {

  def eventType = column[EventType]("event_type", NotNull)

  def userId = column[String]("user_id", NotNull)

  def eventCreated = column[Instant]("event_created", NotNull)

  def eventId = column[UUID]("event_id", O.PrimaryKey, NotNull)

  def assetId = column[String]("asset_id", NotNull)

  def majorVersion = column[Int]("major_version", NotNull)

  def minorVersion = column[Int]("minor_version", NotNull)


  override def * = (eventType, userId, eventCreated, eventId, assetId, majorVersion, minorVersion) <> (Event.tupled, Event.unapply)
}

