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

package v1.connectors

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.{DownstreamTaxYear, Nino}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.amendSample.{AmendSampleRequest, AmendSampleRequestBody}

import scala.concurrent.Future

class AmendSampleConnectorSpec extends ConnectorSpec {

  val nino: String                         = "AA123456A"
  val downstreamTaxYear: DownstreamTaxYear = DownstreamTaxYear.fromMtd(taxYear = "2018-19")

  val request: AmendSampleRequest = AmendSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = downstreamTaxYear,
    body = AmendSampleRequestBody("someData")
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendSampleConnector = new AmendSampleConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AmendSampleConnector" when {
    "amendSample" must {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPut      = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(
            url = s"$baseUrl/some-placeholder/template/$nino/$downstreamTaxYear",
            config = dummyIfsHeaderCarrierConfig,
            body = request.body,
            requiredHeaders = requiredIfsHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.amendSample(request)) shouldBe outcome
      }
    }
  }

}
