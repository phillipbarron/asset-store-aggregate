package bbc.cps.assetstoreaggregate.services

import bbc.camscalatrachassis.concurrent.CustomExecutionContext
import bbc.cps.monitoring.StatsD
import bbc.cps.assetstoreaggregate.Config
import bbc.cps.assetstoreaggregate.exceptions._
import bbc.cps.assetstoreaggregate.model.OptimoEvent
import bbc.cps.assetstoreaggregate.util.JsonFormats
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.{AmazonServiceException, SdkClientException}
import org.json4s.native.Serialization.{read, write}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.MustMatchers._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec}
import util.AppTestUtil

import scala.concurrent.Await
import scala.concurrent.duration._

class SnapshotServiceSpec extends FlatSpec with AppTestUtil with MockitoSugar with BeforeAndAfter with JsonFormats {

  private val mockS3Client = mock[AmazonS3]
  private val mockStatsD = mock[StatsD]

  before {
    reset(mockS3Client, mockStatsD)
  }

  object SnapshotServiceTest extends SnapshotService {
    override val s3Client: AmazonS3 = mockS3Client
  }

  "getSnapshot()" should "retrieve and resolve an existing snapshot" in {
    val snapshotResult = "{ \"foo\": \"bar\" }"
    when(mockS3Client.getObjectAsString(any[String], any[String])) thenReturn snapshotResult

    val result = SnapshotServiceTest.getSnapshot("foo", "bar")

    result mustBe snapshotResult
  }

  it should "throw a suitable exception if the requested snapshot is not there" in {
    val ex = new AmazonServiceException("error message")
    ex.setStatusCode(404)
    ex.setErrorCode("NoSuchKey")

    when(mockS3Client.getObjectAsString(any[String], any[String])) thenThrow ex
    a [SnapshotNotFoundException] should be thrownBy SnapshotServiceTest.getSnapshot("foo", "bar")
  }

  it should "throw a suitable exception if it cannot find the location of the requested snapshot" in {
    val ex = new AmazonServiceException("error message")
    ex.setStatusCode(404)
    ex.setErrorCode("SomethingElse")

    when(mockS3Client.getObjectAsString(any[String], any[String])) thenThrow ex
    a [SnapshotRetrievalFailureException] should be thrownBy SnapshotServiceTest.getSnapshot("foo", "bar")
  }

  it should "throw a suitable exception and if access to the snapshot is forbidden" in {
    val ex = new AmazonServiceException("error message")
    ex.setStatusCode(403)

    when(mockS3Client.getObjectAsString(any[String], any[String])) thenThrow ex
    a [SnapshotRetrievalFailureException] should be thrownBy SnapshotServiceTest.getSnapshot("foo", "bar")
  }

  it should "throw a suitable exception if it cannot connect to the snapshot repository" in {
    when(mockS3Client.getObjectAsString(any[String], any[String])) thenThrow new SdkClientException("error message")
    a [SnapshotRetrievalFailureException] should be thrownBy SnapshotServiceTest.getSnapshot("foo", "bar")
  }

  "saveSnapshot()" should "save a snapshot" in {
    val optimoEvent = read[OptimoEvent](fixtureAsString("saved-message.json"))
    when(mockS3Client.putObject(any[String], any[String], any[String])) thenReturn new PutObjectResult()

    Await.result(SnapshotServiceTest.saveSnapshot(optimoEvent), 5.seconds)

    val expectedKey = s"${Config.environment}/cmw3vglz2j4o/d00b0613-11e2-43a7-a0b1-16f9bc7b7efe.json"
    val expectedContent = write(optimoEvent.data.payload.eventData)
    verify(mockS3Client).putObject(Config.snapshotBucket, expectedKey, expectedContent)
  }

  it should "return a failed future if cannot save the snapshot" in {
    val optimoEvent = read[OptimoEvent](fixtureAsString("saved-message.json"))
    when(mockS3Client.putObject(any[String], any[String], any[String])) thenThrow new SdkClientException("error message")
    ScalaFutures.whenReady(SnapshotServiceTest.saveSnapshot(optimoEvent).failed) {
      e => e mustBe a [SdkClientException]
    }
  }
}
