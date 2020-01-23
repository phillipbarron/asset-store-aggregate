package steps

import bbc.camscalatratestchassis.{ChassisMockServer, PortFinder}
import bbc.cps.camscalatrachassis.steps.ApiStepsChassis
import dispatch.url
import org.scalatest.MustMatchers._

class DocsSteps extends ApiStepsChassis {
  Before { _ =>
    ChassisMockServer.mockServer.reset()
  }

  val host = s"http://localhost:${PortFinder.applicationPort}"

  When("""^I request a static file$"""){ () =>
    get(url(s"$host/css/reset.css"))
  }

  Then("""^the static file is returned$"""){ () =>
    response.getStatusCode mustBe 200
    response.getResponseBody must include("HTML5 display-role reset")
  }
  When("""^I request a static file that doesn't exist$"""){ () =>
    get(url(s"$host/docs/css/unknown.txt"))
  }
  Then("""^I receive a not found response$"""){ () =>
    response.getStatusCode mustBe 404
  }
}
