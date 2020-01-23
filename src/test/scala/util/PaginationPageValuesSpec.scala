package util

import bbc.cps.assetstoreaggregate.util.PaginationPageValues
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

class PaginationPageValuespec extends FlatSpec {

  object PaginationPageValues extends PaginationPageValues


  "offset()" should "calculate the offset value" in {
    PaginationPageValues.offset(page = 1, pageSize = 1) mustBe 0
    PaginationPageValues.offset(page = 2, pageSize = 1) mustBe 1
    PaginationPageValues.offset(page = 2, pageSize = 2) mustBe 2
  }

  "nextPage()" should "return the next page" in {
    PaginationPageValues.nextPage(pageSize = 1, currentPage = 1, totalResults = 1) mustBe None
    PaginationPageValues.nextPage(pageSize = 2, currentPage = 1, totalResults = 2) mustBe None
    PaginationPageValues.nextPage(pageSize = 1, currentPage = 1, totalResults = 2) mustBe Some(2)
    PaginationPageValues.nextPage(pageSize = 5, currentPage = 4, totalResults = 25) mustBe Some(5)
  }
}
