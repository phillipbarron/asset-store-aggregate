package bbc.cps.optimohistoryapi.api

import bbc.cps.optimohistoryapi.exceptions.HistoryNotFoundException
import bbc.cps.optimohistoryapi.model.EventType
import bbc.cps.optimohistoryapi.monitoring.HistoryApiMonitor
import bbc.cps.optimohistoryapi.services.HistoryService
import bbc.cps.optimohistoryapi.util.{DateTimeSerializer, InstantSerializer}
import org.json4s.ext.{EnumNameSerializer, UUIDSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{AsyncResult, NotFound}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


trait HistoryApi extends BaseApi {
  private val log = LoggerFactory getLogger getClass

  override protected implicit val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(EventType) + UUIDSerializer + InstantSerializer + DateTimeSerializer

  private def param(name: String) =
    params.get(name)

  val historyService: HistoryService

  before() {
    contentType = formats("json")
  }

  get("/:assetId") {
    RequestParamsParser(param("assetId"), param("page"), param("size")) match {
      case Success(parsedParams) =>
        log.info(s"Retrieving events for asset ${parsedParams.assetId}")
        new AsyncResult {
          val is = HistoryApiMonitor.timeAsync("route.history") {
            historyService.getHistory(parsedParams)
          }
        }
      case Failure(e) => throw e
    }
  }

  error {
    case e: HistoryNotFoundException =>
      log.info(e.getMessage, e)
      halt(NotFound(buildErrorResponse(e.getMessage)))
  }
}

object HistoryApi extends HistoryApi {
  override val historyService: HistoryService = HistoryService
}
