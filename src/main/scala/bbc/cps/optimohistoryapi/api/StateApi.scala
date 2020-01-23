package bbc.cps.assetstoreaggregate.api

import bbc.cps.assetstoreaggregate.exceptions.{SnapshotNotFoundException, SnapshotRetrievalFailureException}
import bbc.cps.assetstoreaggregate.services.SnapshotService
import org.json4s.JsonAST.{JObject, JString}
import org.scalatra._
import org.slf4j.LoggerFactory


trait StateApi extends BaseApi {

  private val log = LoggerFactory getLogger getClass

  val snapshotService: SnapshotService

  before() {
    contentType = formats("json")
  }

  get("/:assetId/:eventId") {
    val assetId = params("assetId")
    val eventId = params("eventId")

    Ok(snapshotService.getSnapshot(assetId, eventId))
  }

  error {
    case e: SnapshotNotFoundException =>
      val message = e.getMessage
      log.info(message, e)
      NotFound(buildErrorResponse(message))

    case e: SnapshotRetrievalFailureException =>
      val err: ActionResult = e.statusCode match {
        case Some(403) => Forbidden(buildErrorResponse(e.getMessage))
        case _ => InternalServerError(buildErrorResponse(e.getMessage))
      }
      handleError(err, e)
  }

  protected def handleError(actionResult: ActionResult, e: Throwable = new Exception("Unknown Exception")) = {
    log.error(e.getMessage, e)
    actionResult
  }
}



object StateApi extends StateApi {
  override val snapshotService: SnapshotService = SnapshotService
}
