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

package api.endpoints.sample.amend.v1

import api.models.domain.TaxYear
import api.models.errors._
import api.models.errors.v1.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError}
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec

class AmendSampleControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino          = "AA123456A"
    val taxYear       = "2020-21"
    val correlationId = "X-123"

    def uri: String = s"/sample/$nino/$taxYear"

    def downstreamUri: String = s"/some-placeholder/template/$nino/${TaxYear.fromMtd(taxYear).toDownstream}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(authorisedHttpHeaders: _*)
    }

  }

  val requestJson: JsValue = Json.parse(
    s"""
       |{
       |  "data": "someData"
       |}
        """.stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "links": [
      |    {
      |      "href": "/mtd/template/sample/AA123456A/2020-21",
      |      "method": "PUT",
      |      "rel": "amend-sample-rel"
      |    },
      |    {
      |      "href": "/mtd/template/sample/AA123456A/2020-21",
      |      "method": "GET",
      |      "rel": "self"
      |    },
      |    {
      |      "href": "/mtd/template/sample/AA123456A/2020-21",
      |      "method": "DELETE",
      |      "rel": "delete-sample-rel"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "Calling the sample endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(method = DownstreamStub.PUT, uri = downstreamUri)
            .withRequestBody(requestJson)
            .thenReturn(status = NO_CONTENT, None)
        }

        val response: WSResponse = await(request().put(requestJson))
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2017-18", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-16", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2015-17", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream service returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |  "code": "$code",
             |  "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "NOT_FOUND", NOT_FOUND, NotFoundError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
