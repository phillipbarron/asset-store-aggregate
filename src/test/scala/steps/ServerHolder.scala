package steps

import bbc.cps.camscalatrachassis.steps.TestServer
import bbc.cps.assetstoreaggregate.Servlets
import bbc.cps.assetstoreaggregate.api.BaseApi

trait ServerHolder  {
  ServerHolder.started()
}

object ServerHolder extends TestServer[BaseApi] {
  def started(): Unit = init(Servlets())
}