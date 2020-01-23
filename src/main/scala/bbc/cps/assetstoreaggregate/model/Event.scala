package bbc.cps.assetstoreaggregate.model

import java.time.Instant
import java.util.UUID
import bbc.cps.assetstoreaggregate.model.EventType.EventType

case class Event(eventType: EventType,
                 userId: String,
                 eventCreated: Instant,
                 eventId: UUID,
                 assetId: String,
                 majorVersion: Int,
                 minorVersion: Int)
