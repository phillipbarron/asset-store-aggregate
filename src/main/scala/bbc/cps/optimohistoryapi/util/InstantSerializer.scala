package bbc.cps.optimohistoryapi.util

import org.json4s.CustomSerializer
import java.time.Instant
import org.joda.time.DateTime
import org.json4s.JsonAST.JString

case object InstantSerializer extends CustomSerializer[Instant](_ => ({
  case JString(string) => Instant.parse(string)
  }, {
  case dateTime: DateTime => JString(dateTime.toString())
  }
))
