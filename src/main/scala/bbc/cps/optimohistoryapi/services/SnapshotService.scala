package bbc.cps.assetstoreaggregate.services

import bbc.cps.assetstoreaggregate.monitoring.HistoryApiMonitor._
import bbc.camscalatrachassis.concurrent.CustomExecutionContext._
import bbc.cps.assetstoreaggregate.Config
import bbc.cps.assetstoreaggregate.Config.S3Client._
import bbc.cps.assetstoreaggregate.exceptions._
import bbc.cps.assetstoreaggregate.model.{Data, OptimoEvent, Payload}
import bbc.cps.assetstoreaggregate.util.JsonFormats
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import org.json4s.native.Serialization.write

import scala.concurrent.Future

trait SnapshotService extends JsonFormats {

  val s3Client: AmazonS3

  def getSnapshot(assetId: String, eventId: String): String = {
    val path = s"${Config.environment}/$assetId/$eventId.json"
    try {
      s3Client.getObjectAsString(Config.snapshotBucket, path)
    } catch {
      case e: AmazonServiceException if e.getStatusCode == 404 && e.getErrorCode.equals("NoSuchKey") =>
        throw SnapshotNotFoundException(s"Snapshot with eventId [$eventId] does not exist for asset [$assetId]")
      case e: AmazonServiceException if e.getStatusCode == 403 =>
        throw SnapshotRetrievalFailureException(s"Unable to retrieve snapshot due to exception: ${e.getMessage}", Some(e.getStatusCode))
      case e: Exception =>
        throw SnapshotRetrievalFailureException(s"Unable to retrieve snapshot due to exception: ${e.getMessage}")
    }
  }

  def saveSnapshot(assetEvent: OptimoEvent): Future[Unit] = monitor("save-snapshot", "saveSnapshot") {
    val OptimoEvent(Data(payload @ Payload(eventId, _, _, _, _, eventData))) = assetEvent
    val key = s"${Config.environment}/${payload.assetId}/$eventId.json"
    Future {
      s3Client.putObject(Config.snapshotBucket, key, write(eventData))
    }
  }
}

object SnapshotService extends SnapshotService {
  override val s3Client: AmazonS3 = historyS3Client
}
