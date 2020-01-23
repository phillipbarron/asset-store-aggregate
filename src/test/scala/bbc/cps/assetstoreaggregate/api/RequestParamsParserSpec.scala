package bbc.cps.assetstoreaggregate.api

import bbc.cps.assetstoreaggregate.exceptions.BadRequestException
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

import scala.util.{Failure, Success}


class RequestParamsParserSpec extends FlatSpec {

  "RequestParamsParser" should "return request params if all params are provided and valid" in {
    val result = RequestParamsParser(Some("assetId"), Some("1"), Some("1"))
    result mustEqual Success(RequestParams("assetId", 1, 1))
  }

  it should "returns a default for page if it is not provided" in {
    val result = RequestParamsParser(Some("assetId"), None, Some("1"))
    result mustBe Success(RequestParams("assetId", 1, 1))
  }

  it should "treat size as optional" in {
    val result = RequestParamsParser(Some("assetId"), Some("1"), None)
    result mustBe Success(RequestParams("assetId", 1, 10))
  }

  it should "treat both page and size as optional" in {
    val result = RequestParamsParser(Some("assetId"), None, None)
    result mustBe Success(RequestParams("assetId", 1, 10))
  }

  it should "return a failure if assetId is not provided" in {
    val result = RequestParamsParser(None, Some("1"), Some("1"))
    result mustBe Failure(BadRequestException("assetId must be provided"))
  }

  it should "return a failure if page is 0" in {
    val result = RequestParamsParser(Some("assetid"), Some("0"), Some("1"))
    result mustBe Failure(BadRequestException("page must be a positive integer"))
  }

  it should "return a failure if page is less than 0" in {
    val result = RequestParamsParser(Some("assetid"), Some("-1"), Some("1"))
    result mustBe Failure(BadRequestException("page must be a positive integer"))
  }

  it should "return a failure if page is not an int" in {
    val result = RequestParamsParser(Some("assetid"), Some("blah"), Some("1"))
    result mustBe Failure(BadRequestException("page must be a positive integer"))
  }

  it should "return a failure if size is 0" in {
    val result = RequestParamsParser(Some("assetid"), Some("1"), Some("0"))
    result mustBe Failure(BadRequestException("size must be a positive integer no greater than 50"))
  }

  it should "return a failure if size is less than 0" in {
    val result = RequestParamsParser(Some("assetid"), Some("1"), Some("-1"))
    result mustBe Failure(BadRequestException("size must be a positive integer no greater than 50"))
  }

  it should "return a failure if size is greater than 50" in {
    val result = RequestParamsParser(Some("assetid"), Some("1"), Some("51"))
    result mustBe Failure(BadRequestException("size must be a positive integer no greater than 50"))
  }

  it should "return a failure if size is not an int" in {
    val result = RequestParamsParser(Some("assetid"), Some("1"), Some("blah"))
    result mustBe Failure(BadRequestException("size must be a positive integer no greater than 50"))
  }
}
