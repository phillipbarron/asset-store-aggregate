package steps

import bbc.cps.camscalatrachassis.steps.TestServer
import bbc.cps.optimohistoryapi.Servlets
import bbc.cps.optimohistoryapi.api.BaseApi

trait ServerHolder  {
  ServerHolder.started()
}

object ServerHolder extends TestServer[BaseApi] {
  def started(): Unit = init(Servlets())
}