package bbc.cps.assetstoreaggregate

import bbc.cps.assetstoreaggregate.api._

object Servlets {
  def apply(): Map[BaseApi, String] = {
    Map(
      IndexApi -> "/*",
      StateApi -> "/state/*",
      AssetStoreApi -> "/history/*",
      SwaggerUi -> "/docs"
    )
  }
}
