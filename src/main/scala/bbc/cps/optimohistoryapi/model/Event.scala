package bbc.cps.optimohistoryapi.model

import java.time.Instant
import java.util.UUID
import bbc.cps.optimohistoryapi.model.EventType.EventType

case class Event(eventType: EventType,
                 userId: String,
                 eventCreated: Instant,
                 eventId: UUID,
                 assetId: String,
                 majorVersion: Int,
                 minorVersion: Int)
