package steps

import bbc.camscalatratestchassis.ChassisMockServer
import bbc.cps.camscalatrachassis.steps.ApiStepsChassis
import bbc.cps.assetstoreaggregate.Config
import org.json4s.native.JsonMethods._
import org.scalatest.MustMatchers._
import bbc.cps.assetstoreaggregate.util.AmazonS3ClientDummy
import dispatch.url
import org.json4s.DefaultFormats
import util.AppTestUtil

class RetrieveSnapshotSteps extends ApiStepsChassis with AppTestUtil {

  implicit val formats = DefaultFormats

  Before { _ =>
    ChassisMockServer.mockServer.reset()
  }

  Given("""^A snapshot exists for assetId (.*) and eventId (.*)$"""){ (assetId: String, eventId: String) =>
    val fileKey: String = s"${Config.environment}/$assetId/$eventId.json"
    AmazonS3ClientDummy.putObject("myBucket", fileKey, snapshotContentString)
    assert(AmazonS3ClientDummy.getObjectAsString("myBucket", fileKey).equals(snapshotContentString))
  }

  When("""^I retrieve an existing snapshot with assetId (.*) and eventId (.*)$"""){ (assetId: String, eventId: String) =>
    get(url(s"$host/state/$assetId/$eventId"))
  }

  When("""^I request a snapshot that does not exist$"""){ () =>
    get(url(s"$host/state/does/notexist"))
  }

  When("""^I request a snapshot that causes an exception$"""){ () =>
    get(url(s"$host/state/throws/exception"))
  }

  When("""^I request a snapshot where its access is forbidden$"""){ () =>
    get(url(s"$host/state/access/forbidden"))
  }

  Then("""^I receive a JSON response indicating that the snapshot does not exist$""") { () =>
    val errorString = buildSnapshotMissingErrorString("does", "notexist")
    val expectedAsJson = parse(errorString)

    responseBodyAsJson mustBe expectedAsJson
  }

  Then("""^I receive a JSON response indicating that there has been an exception$""") { () =>
    val expectedErrorString = "Unable to retrieve snapshot due to exception"
    val actualErrorString = (responseBodyAsJson \ "error").extract[String]

    actualErrorString must startWith(expectedErrorString)
  }

  Then("""^The status code is (.*)$""") { code: Int =>
    val expectedStatusCode = code
    val actualStatusCode = response.getStatusCode

    actualStatusCode mustBe expectedStatusCode
  }

  Then("""^I am returned that snapshot as JSON$""") { () =>
    val expectedAsJson = parse(snapshotContentString)

    responseBodyAsJson mustBe expectedAsJson
  }

  val snapshotContentString = "{ \"foo\": \"bar\"}"

  def buildSnapshotMissingErrorString (assetId: String, eventId: String): String = {
    s"""{ "error": "Snapshot with eventId [$eventId] does not exist for asset [$assetId]"}"""
  }
}
