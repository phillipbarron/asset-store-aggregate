package steps

import java.time.Instant
import java.util.UUID

import bbc.cps.camscalatrachassis.steps.ApiStepsChassis
import bbc.cps.optimohistoryapi.Config
import bbc.cps.optimohistoryapi.model.{Event, EventType}
import bbc.cps.optimohistoryapi.util.{AmazonS3ClientDummy, AmazonSNSClientDummy}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.MustMatchers._
import util.AppTestUtil
import util.KafkaUtil._

import scala.concurrent.Await
import scala.concurrent.duration._

class OptimoEventsSteps extends ApiStepsChassis with AppTestUtil {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  val createdEventId = "81541c9a-4b86-4f67-b4d1-51afd9c91c1c"
  val savedEventId = "d00b0613-11e2-43a7-a0b1-16f9bc7b7efe"
  val publishedEventId = "5da7a033-475b-49a3-9a51-47299b569f1c"
  val minorChangeEventId = "b9c4be6f-18f3-4157-a3c3-2aed8a455bbc"
  val publishedNewsUpdateId = "3ca1b571-78e6-4b55-be38-17053ab0f1db"
  val assetId = "cmw3vglz2j4o"

  val createdEvent = Event(
    EventType.CREATED,
    "core.engineering@bbc.co.uk",
    Instant.parse("2019-08-05T08:17:17.986Z"),
    UUID.fromString(createdEventId),
    assetId,
    0,
    0
  )

  val publishedEvent = Event(
    EventType.PUBLISHED,
    "core.engineering@bbc.co.uk",
    Instant.parse("2019-08-05T08:17:17.986Z"),
    UUID.fromString(publishedEventId),
    assetId,
    1,
    0
  )

  When("""^a news update is published$""") { () =>
    producerSend(fixtureAsString("news-update-published-message.json"))
  }

  When("""^a published event exists for an article$""") { () =>
    Await.result(saveEvent(publishedEvent), 5.seconds)
  }

  When("""^an article saved message is received$""") { () =>
    producerSend(fixtureAsString("created-message.json"))
  }

  When("""^an article published message is received$""") { () =>
    producerSend(fixtureAsString("published-message.json"))
  }

  When("""^an article minor change message is received$""") { () =>
    producerSend(fixtureAsString("minor-change-message.json"))
  }

  When("""^an invalid message is received$""") { () =>
    producerSend("""{ "bogus": "event" }""")
  }

  When("""^a second article saved message is received$""") { () =>
    producerSend(fixtureAsString("saved-message.json"))
  }

  When("""^two duplicate article published messages are received$""") { () =>
    producerSend(fixtureAsString("published-message.json"))
    producerSend(fixtureAsString("published-message.json"))
  }

  Then("""^a created event is added to the article's history$""") { () =>
    retryAssertions { _ =>
      getArticleEvents(assetId)  mustBe List(createdEvent)
    }
  }

  Then("""^a published event is added to the article's history$""") { () =>
    retryAssertions { _ =>
      getArticleEvents(assetId)  mustBe List(publishedEvent)
    }
  }

  Then("""^a saved event is added to the article's history$""") { () =>
    val expectedSavedEvent = Event(
      EventType.SAVED,
      "core.engineering@bbc.co.uk",
      Instant.parse("2019-08-05T08:17:17.986Z"),
      UUID.fromString(savedEventId),
      assetId,
      0,
      0
    )

    retryAssertions { _ =>
      getArticleEvents(assetId)  mustBe List(createdEvent, expectedSavedEvent)
    }
  }

  Then("""^a published minor change event is added to the article's history$""") { () =>
    val expectedMinorChangeEvent = Event(
      EventType.PUBLISHED_MINOR_CHANGE,
      "core.engineering@bbc.co.uk",
      Instant.parse("2019-08-05T08:17:17.986Z"),
      UUID.fromString(minorChangeEventId),
      assetId,
      1,
      1
    )

    retryAssertions { _ =>
      getArticleEvents(assetId)  mustBe List(publishedEvent, expectedMinorChangeEvent)
    }
  }

  Then("""^a news update published event is added to the article's history$""") { () =>
    val expectedNewsUpdateChangeEvent = Event(
      EventType.PUBLISHED_NEWS_UPDATE,
      "core.engineering@bbc.co.uk",
      Instant.parse("2019-08-05T08:17:17.986Z"),
      UUID.fromString(publishedNewsUpdateId),
      assetId,
      2,
      0
    )

    retryAssertions { _ =>
      getArticleEvents(assetId)  mustBe List(publishedEvent, expectedNewsUpdateChangeEvent)
    }
  }

  Then("""^a snapshot is saved""") { () =>
    retryAssertions { _ =>
      val fileKey = s"${Config.environment}/$assetId/$createdEventId.json"
      val snapshotContentString = write(fixtureAsJson("created-message.json") \ "data" \ "payload" \ "eventData")
      assert(AmazonS3ClientDummy.getObjectAsString(Config.snapshotBucket, fileKey).equals(snapshotContentString))
    }
  }

  Then("""^a history updated notification is sent$"""){ () =>
    val expectedNotification = JObject(
      "type" -> JString("history"),
      "assetId" -> JString(assetId)
    )
    AmazonSNSClientDummy.latestNotification mustBe expectedNotification
  }
}
