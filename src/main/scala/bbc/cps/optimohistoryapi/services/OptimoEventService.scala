package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor._
import bbc.cps.assetstoreaggregate.model.OptimoEvent
import org.slf4j.LoggerFactory
import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import scala.concurrent.Future


trait OptimoEventService {
  protected val eventService: EventService
  protected val snapshotService: SnapshotService
  protected val notificationService: NotificationService
  private val log = LoggerFactory getLogger getClass

  private def futureWithEither(future: Future[Unit]) =
    future map (_ => Right(())) recover {
      case e => Left(e)
    }

  private def logError(errorKey: String, optimoEvent: OptimoEvent, error: Throwable): Unit = {
    incrementMetric(s"errors-save-$errorKey", s"errors.save$errorKey")
    log.error(s"failed to write $errorKey $optimoEvent", error)
  }

  private def logAnyError(optimoEvent: OptimoEvent, saveEventResult: Either[Throwable, Unit], saveSnapshotResult: Either[Throwable, Unit]) = {
    if (saveEventResult.isLeft) {
      logError("event", optimoEvent, saveEventResult.left.get)
    }

    if (saveSnapshotResult.isLeft) {
      logError("snapshot", optimoEvent, saveSnapshotResult.left.get)
    }
  }

  def processEvent(optimoEvent: OptimoEvent): Future[Unit] = {
    val saveEventFuture = eventService.saveEvent(optimoEvent)
    val saveSnapshotFuture = snapshotService.saveSnapshot(optimoEvent)
    for {
      saveEventResult <- futureWithEither(saveEventFuture)
      saveSnapshotResult <- futureWithEither(saveSnapshotFuture)
    } yield {
      logAnyError(optimoEvent, saveEventResult, saveSnapshotResult)
      if (saveEventResult.isRight) {
        notificationService.sendHistoryChangeNotification(optimoEvent.data.payload.assetId)
      }
      Future.successful(())
    }
  }
}

object OptimoEventService extends OptimoEventService {
  protected val eventService: EventService = EventService
  protected val snapshotService: SnapshotService = SnapshotService
  protected val notificationService : NotificationService = NotificationService
}
