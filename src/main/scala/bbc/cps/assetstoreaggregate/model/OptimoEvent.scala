package bbc.cps.assetstoreaggregate.model

import java.util.UUID
import bbc.cps.assetstoreaggregate.model.EventType.EventType
import java.time.Instant
import org.json4s.JsonAST.JValue

case class OptimoEvent(data: Data)

case class Data(payload: Payload)

case class Payload(eventId: UUID,
                   eventType: EventType,
                   eventTimestamp: Instant,
                   entityId: String,
                   userId: String,
                   eventData: JValue) {
  val assetId: String = entityId.split(":").last
}
