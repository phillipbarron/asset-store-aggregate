package bbc.cps.optimohistoryapi.monitoring

import bbc.camscalatrachassis.monitoring.MonitorChassis
import bbc.cps.optimohistoryapi.Config

object HistoryApiMonitor extends MonitorChassis {
  val applicationName = Config.applicationName
  def environment = Config.environment
}
