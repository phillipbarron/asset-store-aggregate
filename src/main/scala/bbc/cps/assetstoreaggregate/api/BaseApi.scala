package bbc.cps.assetstoreaggregate.api

import bbc.camscalatrachassis.api.ApiChassis
import bbc.cps.assetstoreaggregate.Config
import bbc.cps.assetstoreaggregate.exceptions.BadRequestException
import org.json4s.JsonAST.{JObject, JString}
import org.scalatra.{BadRequest, InternalServerError}
import org.slf4j.LoggerFactory

trait BaseApi extends ApiChassis {
  implicit val applicationName: String = Config.applicationName
  private val log = LoggerFactory getLogger getClass

  protected def buildErrorResponse(message: String) = JObject("error" -> JString(message))

  error {
    case e: BadRequestException =>
      log.error(e.getMessage, e)
      halt(BadRequest(buildErrorResponse(e.getMessage)))
    case e: Exception =>
      log.error(e.getMessage, e)
      halt(InternalServerError(buildErrorResponse(e.getMessage)))
  }
}