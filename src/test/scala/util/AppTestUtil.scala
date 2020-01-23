package util

import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.camscalatratestchassis.{PortFinder, TestUtil}
import bbc.cps.optimohistoryapi.Config
import bbc.cps.optimohistoryapi.Config.Postgres
import bbc.cps.optimohistoryapi.model.{Event, EventsTable}
import bbc.cps.optimohistoryapi.util.SlickTypeExtensionProfile.api._
import slick.dbio.{Effect, NoStream}
import slick.lifted.TableQuery
import slick.sql.SqlAction

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait AppTestUtil extends TestUtil {
  val host = s"http://localhost:${PortFinder.applicationPort}"
  implicit def applicationName = Config.applicationName
  private val events = TableQuery[EventsTable]

  def executeQuery(sqlQuery: SqlAction[Int, NoStream, Effect]) = {
    Await.result(Config.Postgres.db.run(sqlQuery), 5.seconds)
  }

  def getArticleEvents(assetId: String): List[Event] = {
    val run = Postgres.db.run(events.filter(_.assetId === assetId).result.map(_.toList)) recoverWith {
      case e =>
        Future.successful(List.empty)
    }
    Await.result(run, 10.seconds)
  }

  def saveEvent(event: Event): Future[Int] = {
    Postgres.db.run(events += event)
  }
}
