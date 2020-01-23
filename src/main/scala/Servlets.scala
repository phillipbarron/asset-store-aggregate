package bbc.cps.optimohistoryapi

import bbc.cps.optimohistoryapi.api._

object Servlets {
  def apply(): Map[BaseApi, String] = {
    Map(
      IndexApi -> "/*",
      StateApi -> "/state/*",
      HistoryApi -> "/history/*",
      SwaggerUi -> "/docs"
    )
  }
}
