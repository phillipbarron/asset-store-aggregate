package steps

import bbc.camscalatratestchassis.ChassisMockServer
import bbc.cps.camscalatrachassis.steps.ApiStepsChassis
import dispatch.url
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.scalatest.MustMatchers._
import slick.jdbc.PostgresProfile.api._
import util.AppTestUtil

class RetrieveHistorySteps extends ApiStepsChassis with AppTestUtil {

  implicit val formats: DefaultFormats.type = DefaultFormats

  private def errorResponse(message: String) =
    s"""{ "error": "$message"}"""

  private val asset1Id = "cq915kk4wy5o"
  private val asset2Id = "c4m6y19xw8po"

  Before { _ =>
    ChassisMockServer.mockServer.reset()
  }

  val asset1Events = List(
    ("SAVED", "some.one@bbc.co.uk", "2019-06-24T00:09:00.985Z", "1519bbd3-256f-42af-8ac7-8d4b677b7789", asset1Id, 0, 0),
    ("SAVED", "some.one@bbc.co.uk", "2019-06-24T01:09:00.984Z", "cd5ed3ba-5dee-4907-9538-31e91f57c5a4", asset1Id, 0, 0),
    ("PUBLISHED", "some.one@bbc.co.uk", "2019-06-24T04:09:00.983Z", "a937694a-f9be-4d5b-9f68-d81e58c76ca3", asset1Id, 1, 0),
    ("PUBLISHED_MINOR_CHANGE", "some.one@bbc.co.uk", "2019-06-24T05:09:00.982Z", "dae6dab7-86da-4105-8825-afd3c2fe37a0", asset1Id, 1, 1),
    ("PUBLISHED", "some.one@bbc.co.uk", "2019-06-24T06:09:00.982Z", "2b1a95cb-e834-4e0a-b5ab-6a58dffa850d", asset1Id, 2, 0),
    ("PUBLISHED", "some.one@bbc.co.uk", "2019-06-24T07:09:00.982Z", "ca01bd9c-5543-4563-a177-46b21f86fae3", asset1Id, 3, 0),
    ("WITHDRAWN", "some.one@bbc.co.uk", "2019-06-25T07:09:00.982Z", "ca01bd9c-5543-4563-a177-46b21f86fae4", asset1Id, 3, 1)
  )

  val asset2Events = List(
    ("SAVED", "some.one@bbc.co.uk", "2019-06-24T00:09:00.981Z", "ea1fe21f-d838-4abb-ae93-d0d8d782675f", asset2Id, 0, 0)
  )

  Given("""^the database is empty$""") { () =>
    executeQuery(sqlu"DROP TABLE IF EXISTS events;")
  }

  Given("""^the relevant tables exist$""") { () =>
    executeQuery(sqlu"""CREATE TABLE IF NOT EXISTS events(
                  event_type VARCHAR(40) NOT NULL,
                  user_id VARCHAR(255) NOT NULL,
                  event_created TIMESTAMP NOT NULL,
                  event_id uuid NOT NULL PRIMARY KEY,
                  asset_id VARCHAR(20) NOT NULL,
                  major_version INT NOT NULL,
                  minor_version INT NOT NULL
                )""")
  }

  Given("""^asset events exist$""") { () =>
    asset2Events.foreach({
      case (eventType, userId, eventCreated, eventId, assetId, majorVersion, minorVersion) =>
        executeQuery(sqlu"INSERT INTO events (event_type, user_id, event_created, event_id, asset_id, major_version, minor_version) VALUES ($eventType, $userId, '#$eventCreated', '#$eventId', $assetId, $majorVersion, $minorVersion)")
    })
  }

  Given("""^A history of events exists for an asset$""") { () =>
    asset1Events.foreach({
      case (eventType, userId, eventCreated, eventId, assetId, majorVersion, minorVersion) =>
        executeQuery(sqlu"INSERT INTO events (event_type, user_id, event_created, event_id, asset_id, major_version, minor_version) VALUES ($eventType, $userId, '#$eventCreated', '#$eventId', $assetId, $majorVersion, $minorVersion)")
    })
  }

  Given("""^an asset has no history$""") {}

  Given("""^the database model has changed$""") { () =>
    executeQuery(sqlu"DROP TABLE IF EXISTS events; CREATE TABLE IF NOT EXISTS events (invalid_field VARCHAR(40) NOT NULL);")
  }

  Given("""^a history event exists that corresponds to the new model$""") { () =>
    executeQuery(sqlu"INSERT INTO events (invalid_field) VALUES ('cn5tyj5aklto');")
  }
  
  When("""^I retrieve the history of the asset with the default values for page and size$""") { () =>
    get(url(s"$host/history/$asset1Id"))
  }

  When("""^I retrieve the history of the asset with page (.*) and size (.*)$"""){ (page: String, size: String) =>
    get(url(s"$host/history/$asset1Id"), Map("page" -> Seq(page), "size" -> Seq(size)))
  }

  When("""^I retrieve the history of an asset providing an invalid page$""") { () =>
    get(url(s"$host/history/$asset1Id"), Map("page" -> Seq("0")))
  }

  Then("""I am returned the history as a list of events in a single page$""") { () =>
    response.getStatusCode mustBe 200
    responseBodyAsJson mustBe fixtureAsJson("asset_history_page_1_of_1_default_size_10_response.json")
  }

  Then("""I am returned page (.*) as a list of events with size (.*) and (?:with a next page value|no next page value)$""") { (page: Int, size: Int) =>
    response.getStatusCode mustBe 200
    responseBodyAsJson mustBe fixtureAsJson(s"asset_history_page_${page}_of_4_size_${size}_response.json")
  }

  Then("""I am told that the history for the asset does not exist$""") { () =>
    response.getStatusCode mustBe 404
    val errorString = errorResponse(s"History for asset [$asset1Id] at page [1] with size [10] does not exist")
    val expectedAsJson = parse(errorString)

    responseBodyAsJson mustBe expectedAsJson
  }

  Then("""^I am told the model is incorrect$""") { () =>
    response.getStatusCode mustBe 500
    val errorString = errorResponse(s"An error occurred when retrieving history for asset $asset1Id")
    val expectedAsJson = parse(errorString)
    responseBodyAsJson mustBe expectedAsJson
  }

  Then("""^I am told the request parameters are invalid$""") { () =>
    response.getStatusCode mustBe 400
    val errorString = errorResponse("page must be a positive integer")
    val expectedAsJson = parse(errorString)

    responseBodyAsJson mustBe expectedAsJson
  }
}
