package bbc.cps.assetstoreaggregate.util

import bbc.cps.assetstoreaggregate.model.EventType
import bbc.cps.assetstoreaggregate.model.EventType.EventType
import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgJson4sSupport, PgJsonSupport}
import org.json4s.JValue
import slick.ast.BaseTypedType
import slick.basic.Capability
import slick.jdbc.{JdbcCapabilities, JdbcType}

trait SlickTypeExtensionProfile extends ExPostgresProfile
  with PgArraySupport
  with PgJson4sSupport {

  def pgjson = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  type DOCType = JValue
  override val jsonMethods = org.json4s.jackson.JsonMethods

  override val api = SlickTypeExtensionAPI

  object SlickTypeExtensionAPI extends API
    with ArrayImplicits
    with Json4sJsonImplicits {

    implicit val eventTypeMapper: JdbcType[EventType] with BaseTypedType[EventType] = MappedColumnType.base[EventType, String](
      e => e.toString,
      s => EventType.withName(s)
    )
  }


}

object SlickTypeExtensionProfile extends SlickTypeExtensionProfile with PgJsonSupport
