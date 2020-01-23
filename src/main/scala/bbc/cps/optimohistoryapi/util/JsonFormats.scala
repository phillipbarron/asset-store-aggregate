package bbc.cps.optimohistoryapi.util

import bbc.cps.optimohistoryapi.model.EventType
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.{EnumNameSerializer, UUIDSerializer}

trait JsonFormats {
  protected implicit lazy val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(EventType) + UUIDSerializer + InstantSerializer + DateTimeSerializer
}
