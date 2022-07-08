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

package api.downstream.sample.connectors

import api.downstream.common.connectors.{ConnectorSpec, MockHttpClient}
import api.endpoints.sample.amend.v1.request.{AmendSampleRequest, AmendSampleRequestBody}
import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.endpoints.sample.retrieve.v1.request.RetrieveSampleRequest
import api.endpoints.sample.retrieve.v1.response.SampleObject
import api.models.ResponseWrapper
import api.models.domain.{Nino, TaxYear}
import config.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SampleConnectorSpec extends ConnectorSpec {

  val nino: String               = "AA123456A"
  val downstreamTaxYear: TaxYear = TaxYear.fromMtd(taxYear = "2018-19")

  val amendRequest: AmendSampleRequest = AmendSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = downstreamTaxYear,
    body = AmendSampleRequestBody("someData")
  )

  val retrieveRequest: RetrieveSampleRequest = RetrieveSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = downstreamTaxYear
  )

  val deleteRequest: DeleteSampleRequest = DeleteSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = downstreamTaxYear
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: SampleConnector = new SampleConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "SampleConnector" when {
    "amendSample" must {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPut      = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(
            url = s"$baseUrl/some-placeholder/template/$nino/${downstreamTaxYear.toDownstream}",
            config = dummyIfsHeaderCarrierConfig,
            body = amendRequest.body,
            requiredHeaders = requiredIfsHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.amendSample(amendRequest)) shouldBe outcome
      }
    }

    "delete" must {
      "return a 204 status for a success scenario" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockHttpClient
          .delete(
            url = s"$baseUrl/some-placeholder/template/$nino/${downstreamTaxYear.toDownstream}",
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.deleteSample(deleteRequest)) shouldBe outcome
      }
    }

    "retrieve" must {
      "return a 200 status for a success scenario" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, SampleObject(dateSubmitted = "value", submissionItem = None)))

        MockHttpClient
          .get(
            url = s"$baseUrl/some-placeholder/template/$nino/${downstreamTaxYear.toDownstream}",
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveSample(retrieveRequest)) shouldBe outcome
      }
    }

  }

}
