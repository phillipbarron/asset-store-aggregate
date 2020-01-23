package bbc.cps.assetstoreaggregate

import bbc.cps.assetstoreaggregate.api._

object Servlets {
  def apply(): Map[BaseApi, String] = {
    Map(
      IndexApi -> "/*",
      AssetStoreApi -> "/history/*",
      SwaggerUi -> "/docs"
    )
  }
}

