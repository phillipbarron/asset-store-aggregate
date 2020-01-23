package bbc.cps.assetstoreaggregate.util

import bbc.cps.assetstoreaggregate.model.EventType
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.{EnumNameSerializer, UUIDSerializer}

trait JsonFormats {
  protected implicit lazy val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(EventType) + UUIDSerializer + InstantSerializer + DateTimeSerializer
}
