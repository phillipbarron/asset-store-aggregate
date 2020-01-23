package bbc.cps.optimohistoryapi.api

import bbc.cps.optimohistoryapi.exceptions.BadRequestException

import scala.util.{Failure, Success, Try}

case class RequestParams(assetId: String, page: Int, pageSize: Int)

object RequestParamsParser {
  private def validateAssetId(assetId: Option[String]) =
    assetId match {
      case Some(id) => Success(id)
      case None => Failure(BadRequestException("assetId must be provided"))
    }

  private def validateInt(string: Option[String], withinBounds: Int => Boolean, default: Int, errorMessage: String) =
    string match {
      case Some(s) => Try { s.toInt } match {
        case Success(i) if withinBounds(i) => Success(i)
        case _ => Failure(BadRequestException(errorMessage))
      }
      case None => Success(default)
    }

  def apply(assetId: Option[String], page: Option[String], size: Option[String]): Try[RequestParams] =
    for {
      validAssetId <- validateAssetId(assetId)
      validPage <- validateInt(page, _ > 0, 1, "page must be a positive integer")
      validSize <- validateInt(size, i => i > 0 && i <= 50, 10, "size must be a positive integer no greater than 50")
    } yield RequestParams(validAssetId, validPage, validSize)
}
