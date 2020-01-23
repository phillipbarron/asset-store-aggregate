package bbc.cps.assetstoreaggregate.monitoring

import bbc.camscalatrachassis.monitoring.MonitorChassis
import bbc.cps.assetstoreaggregate.Config

object HistoryApiMonitor extends MonitorChassis {
  val applicationName = Config.applicationName
  def environment = Config.environment
}
