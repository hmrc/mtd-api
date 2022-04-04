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

package api.connectors

import api.connectors.DownstreamUri.IfsUri
import api.mocks.MockHttpClient
import api.models.outcomes.ResponseWrapper
import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.Future

class BaseDownstreamConnectorSpec extends ConnectorSpec {

  // WLOG
  case class Result(value: Int)

  // WLOG
  val body = "body"

  val outcome = Right(ResponseWrapper(correlationId, Result(2)))

  val url         = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  implicit val httpReads: HttpReads[DownstreamOutcome[Result]] = mock[HttpReads[DownstreamOutcome[Result]]]

  class Test(downstreamEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns downstreamEnvironmentHeaders
  }

  "BaseDownstreamConnector" when {
    val requiredHeaders: Seq[(String, String)] = Seq(
      "Environment"   -> "ifs-environment",
      "Authorization" -> s"Bearer ifs-token"
    )

    val excludedHeaders: Seq[(String, String)] = Seq(
      "AnotherHeader" -> "HeaderValue"
    )

    "making a HTTP request to the downstream service (IFS)" must {
      testHttpMethods(dummyIfsHeaderCarrierConfig, requiredHeaders, excludedHeaders, Some(allowedIfsHeaders))

      "exclude all `otherHeaders` when no external service header allow-list is found" should {
        val requiredHeaders: Seq[(String, String)] = Seq(
          "Environment"   -> "ifs-environment",
          "Authorization" -> s"Bearer ifs-token"
        )

        testHttpMethods(dummyIfsHeaderCarrierConfig, requiredHeaders, otherHeaders, None)
      }
    }
  }

  def testHttpMethods(config: HeaderCarrier.Config,
                      requiredHeaders: Seq[(String, String)],
                      excludedHeaders: Seq[(String, String)],
                      downstreamEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "POST" in new Test(downstreamEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, IfsUri[Result](url))) shouldBe outcome
      }

      "GET" in new Test(downstreamEnvironmentHeaders) {
        MockHttpClient
          .get(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.get(IfsUri[Result](url))) shouldBe outcome
      }

      "DELETE" in new Test(downstreamEnvironmentHeaders) {
        MockHttpClient
          .delete(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.delete(IfsUri[Result](url))) shouldBe outcome
      }

      "PUT" in new Test(downstreamEnvironmentHeaders) {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))

        val requiredHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(absoluteUrl, config, body, requiredHeadersPut, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.put(body, IfsUri[Result](url))) shouldBe outcome
      }
    }
  }

}
