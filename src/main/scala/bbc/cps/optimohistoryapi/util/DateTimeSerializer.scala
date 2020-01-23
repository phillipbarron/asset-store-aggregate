package bbc.cps.assetstoreaggregate.util

import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

case object DateTimeSerializer extends CustomSerializer[DateTime](format => ({
  case JString(string) => new DateTime(string, ISOChronology.getInstanceUTC)
  }, {
    case dateTime: DateTime => JString(dateTime.toString())
  }
))
