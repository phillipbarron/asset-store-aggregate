import bbc.camscalatrachassis.lifecycle.ScalatraBootstrapChassis
import bbc.cps.assetstoreaggregate.api.BaseApi
import bbc.cps.assetstoreaggregate.dao.OptimoKafkaConsumer
import bbc.cps.assetstoreaggregate.{Config, Servlets}

class ScalatraBootstrap extends ScalatraBootstrapChassis[BaseApi] {
  protected val applicationName: String = Config.applicationName
  protected val servlets: Map[BaseApi, String] = Servlets()
  protected val metricsNamespace = "BBCApp/assetstoreaggregate"
  OptimoKafkaConsumer.consume()
}
