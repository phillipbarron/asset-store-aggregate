package util

import bbc.camscalatratestchassis.{PortFinder, TestUtil}
import bbc.cps.assetstoreaggregate.Config


trait AppTestUtil extends TestUtil {
  val host = s"http://localhost:${PortFinder.applicationPort}"
  implicit def applicationName = Config.applicationName
}
