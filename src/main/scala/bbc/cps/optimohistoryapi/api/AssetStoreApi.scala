package bbc.cps.assetstoreaggregate.api

import bbc.cps.assetstoreaggregate.exceptions.HistoryNotFoundException
import bbc.cps.assetstoreaggregate.model.EventType
import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor
import bbc.cps.assetstoreaggregate.services.AssetStoreApi
import bbc.cps.assetstoreaggregate.util.{DateTimeSerializer, InstantSerializer}
import org.json4s.ext.{EnumNameSerializer, UUIDSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{AsyncResult, NotFound}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


trait AssetStoreApi extends BaseApi {
  private val log = LoggerFactory getLogger getClass

  override protected implicit val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(EventType) + UUIDSerializer + InstantSerializer + DateTimeSerializer

  private def param(name: String) =
    params.get(name)

  val historyService: AssetStoreApi

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
  override val historyService: AssetStoreApi = AssetStoreApi
}
