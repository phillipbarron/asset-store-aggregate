package steps

import bbc.camscalatratestchassis.{ChassisMockServer, PortFinder}
import bbc.cps.camscalatrachassis.steps.ApiStepsChassis
import dispatch.url
import org.scalatest.MustMatchers._

class HttpSteps extends ApiStepsChassis with ServerHolder {
  Before { _ =>
    ChassisMockServer.mockServer.reset()
  }

  val host = s"http://localhost:${PortFinder.applicationPort}"

  When("""^I request the application status$""") { () =>
    get(url(s"$host/status"))
  }

  When("""^I request the application root$""") { () =>
    get(url(s"$host"))
  }

  Then("""^a healthy status is returned$""") { () =>
    response.getStatusCode mustBe 200
  }
}
