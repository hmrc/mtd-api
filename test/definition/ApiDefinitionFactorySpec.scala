/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package definition

import config.ConfidenceLevelConfig
import definition.APIStatus.{ALPHA, BETA}
import mocks.MockAppConfig
import routing.Version1
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec with MockAppConfig {

  class Test {
    val factory = new ApiDefinitionFactory(mockAppConfig)
  }

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  "definition" when {
    "there is no appConfig.apiStatus" should {
      "default apiStatus to ALPHA" in new Test {
        MockAppConfig.apiGatewayContext returns "mtd/template"
        MockAppConfig.apiStatus(Version1) returns "" anyNumberOfTimes ()
        MockAppConfig.endpointsEnabled(version = Version1.name) returns false anyNumberOfTimes ()
        MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(definitionEnabled = true, authValidationEnabled = true) anyNumberOfTimes ()

        private val readScope  = "read:self-assessment"
        private val writeScope = "write:self-assessment"

        factory.definition shouldBe
          Definition(
            scopes = Seq(
              Scope(
                key = readScope,
                name = "View your Self Assessment information",
                description = "Allow read access to self assessment data",
                confidenceLevel
              ),
              Scope(
                key = writeScope,
                name = "Change your Self Assessment information",
                description = "Allow write access to self assessment data",
                confidenceLevel
              )
            ),
            api = APIDefinition(
              name = "#mtd-api# (MTD)",
              description = "#desc#",
              context = "mtd/template",
              versions = Seq(
                APIVersion(
                  version = Version1,
                  status = ALPHA,
                  endpointsEnabled = false
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    val anyVersion = Version1
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockAppConfig.apiStatus(version = anyVersion) returns "BETA"
        factory.buildAPIStatus(version = anyVersion) shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockAppConfig.apiStatus(version = anyVersion) returns "ALPHO"
        factory.buildAPIStatus(version = anyVersion) shouldBe ALPHA
      }
    }
  }

}
