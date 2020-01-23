import bbc.camscalatrachassis.lifecycle.ScalatraBootstrapChassis
import bbc.cps.optimohistoryapi.api.BaseApi
import bbc.cps.optimohistoryapi.dao.OptimoKafkaConsumer
import bbc.cps.optimohistoryapi.{Config, Servlets}


class ScalatraBootstrap extends ScalatraBootstrapChassis[BaseApi] {
  protected val applicationName = Config.applicationName
  protected val servlets = Servlets()
  protected val metricsNamespace = "BBCApp/optimohistoryapi"
  OptimoKafkaConsumer.consume()
}
