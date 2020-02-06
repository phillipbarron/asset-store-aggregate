package bbc.cps.assetstoreaggregate.api

import bbc.cps.assetstoreaggregate.exceptions.AssetNotFoundException
import bbc.cps.assetstoreaggregate.model.{Branch, EventType}
import bbc.cps.assetstoreaggregate.services.AssetStoreService
import bbc.cps.assetstoreaggregate.util.{DateTimeSerializer, InstantSerializer}
import org.json4s.ext.{EnumNameSerializer, UUIDSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{NotFound, Ok}
import org.slf4j.LoggerFactory

trait AssetStoreApi extends BaseApi {
  private val log = LoggerFactory getLogger getClass

  override protected implicit val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(EventType) + UUIDSerializer + InstantSerializer + DateTimeSerializer

  val assetStoreService: AssetStoreService

  before() {
    contentType = formats("json")
  }

  get("/:id") {
   assetStoreService.getAsset(params("id"))
  }

  get("/:id/branch/:branch") {
    assetStoreService.getAssetBranch(params("id"), ("branch"))
  }

  error {
    case e: AssetNotFoundException =>
      log.info(e.getMessage, e)
      halt(NotFound(buildErrorResponse(e.getMessage)))
  }
}

object AssetStoreApi extends AssetStoreApi {
  override val assetStoreService: AssetStoreService = AssetStoreService
}
