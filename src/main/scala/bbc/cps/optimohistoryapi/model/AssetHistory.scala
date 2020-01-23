package bbc.cps.optimohistoryapi.model

import java.util.UUID

import bbc.cps.optimohistoryapi.model.EventType.EventType
import org.joda.time.DateTime

case class AssetHistory(assetId: String,
                                 versions: List[VersionHistory],
                                 pagination: PaginationInfo )

case class VersionHistory(majorVersion: Int,
                          events: List[HistoryEvent])

case class HistoryEvent(eventId: UUID,
                        `type`: EventType,
                        timestamp: DateTime,
                        userId: String,
                        minorVersion: Int)
