package bbc.cps.assetstoreaggregate.util

trait PaginationPageValues {

  def offset(page:Int, pageSize:Int): Int = (page - 1) * pageSize

  def nextPage(pageSize: Int, currentPage: Int, totalResults: Int): Option[Int] = {
    val hasNextPage = totalResults - (offset(currentPage, pageSize) + pageSize) > 0

    hasNextPage match {
      case true => Some(currentPage + 1)
      case false => None
    }
  }
}