package bbc.cps.optimohistoryapi.api

class IndexApi extends BaseApi {

  get("/") {
    contentType = "application/json"
    """{"application":"optimo-history-api"}"""
  }

  get("/status") {
    contentType = "application/json"
    """{"status":"OK"}"""
  }
}

object IndexApi extends IndexApi