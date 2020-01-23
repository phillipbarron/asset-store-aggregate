package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.model.OptimoEvent
import org.slf4j.LoggerFactory
import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import scala.concurrent.Future


trait OptimoEventService {
  protected val assetStoreService: AssetStoreService
  protected val notificationService: NotificationService
  private val log = LoggerFactory getLogger getClass

  private def futureWithEither(future: Future[Unit]) =
    future map (_ => Right(())) recover {
      case e => Left(e)
    }


  def processEvent(optimoEvent: OptimoEvent): Future[Unit] = {
    assetStoreService.saveEvent(optimoEvent)
  }
}

object OptimoEventService extends OptimoEventService {
  protected val assetStoreService: AssetStoreService = AssetStoreService
  protected val notificationService : NotificationService = NotificationService
}
