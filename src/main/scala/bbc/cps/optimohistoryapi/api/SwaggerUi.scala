package bbc.cps.assetstoreaggregate.api

trait SwaggerUi extends BaseApi {

  before() {
    contentType = "text/html"
  }

  get("/") {
    getClass.getResourceAsStream("/index.html")
  }

}


object SwaggerUi extends SwaggerUi{}
