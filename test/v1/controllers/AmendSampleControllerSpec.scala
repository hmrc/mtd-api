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

package v1.controllers

import api.controllers.ControllerBaseSpec
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{DownstreamTaxYear, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockAmendSampleRequestParser
import v1.mocks.services.MockAmendSampleService
import v1.models.request.amendSample.{AmendSampleRawData, AmendSampleRequest, AmendSampleRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendSampleControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendSampleService
    with MockAmendSampleRequestParser
    with MockAuditService {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2017-18"
  val correlationId: String = "X-123"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "data" : "someData"
      |}
    """.stripMargin
  )

  val rawData: AmendSampleRawData = AmendSampleRawData(
    nino = nino,
    taxYear = taxYear,
    body = requestBodyJson
  )

  val requestBody: AmendSampleRequestBody = AmendSampleRequestBody(
    data = "someData"
  )

  val requestData: AmendSampleRequest = AmendSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = DownstreamTaxYear.fromMtd(taxYear),
    body = requestBody
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendSampleController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendSampleRequestParser,
      service = mockAmendSampleService,
      auditService = mockAuditService,
      cc = cc
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockEnrolmentsAuthService.authoriseUser()
    MockAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
  }

  val responseBody: JsValue = Json.parse(
    """
      |{
      |  "links": [
      |    {
      |      "href": "/baseUrl/sample/AA123456A/2017-18",
      |      "method": "PUT",
      |      "rel": "amend-sample-rel"
      |    },
      |    {
      |      "href": "/baseUrl/sample/AA123456A/2017-18",
      |      "method": "GET",
      |      "rel": "self"
      |    },
      |    {
      |      "href": "/baseUrl/sample/AA123456A/2017-18",
      |      "method": "DELETE",
      |      "rel": "delete-sample-rel"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "AmendSampleController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendSampleRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendSampleService
          .amendSample(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendSample(nino, taxYear)(
          fakePutRequest(requestBodyJson)
        )

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val detail: GenericAuditDetail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino = nino,
          taxYear = taxYear,
          `X-CorrelationId` = correlationId,
          response = AuditResponse(
            httpStatus = OK,
            errors = None
          )
        )

        val event: AuditEvent[GenericAuditDetail] = AuditEvent[GenericAuditDetail](
          auditType = "sampleAuditType",
          transactionName = "sample-transaction-type",
          detail = detail
        )

        MockAuditService.verifyAuditEvent(event).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendSampleRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.amendSample(nino, taxYear)(
              fakePutRequest(requestBodyJson)
            )

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val detail: GenericAuditDetail = GenericAuditDetail(
              userType = "Individual",
              agentReferenceNumber = None,
              nino = nino,
              taxYear = taxYear,
              `X-CorrelationId` = header("X-CorrelationId", result).get,
              response = AuditResponse(
                httpStatus = expectedStatus,
                errors = Some(Seq(AuditError(error.code)))
              )
            )

            val event: AuditEvent[GenericAuditDetail] = AuditEvent[GenericAuditDetail](
              auditType = "sampleAuditType",
              transactionName = "sample-transaction-type",
              detail = detail
            )

            MockAuditService.verifyAuditEvent(event).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendSampleRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendSampleService
              .amendSample(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.amendSample(nino, taxYear)(
              fakePutRequest(requestBodyJson)
            )

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val detail: GenericAuditDetail = GenericAuditDetail(
              userType = "Individual",
              agentReferenceNumber = None,
              nino = nino,
              taxYear = taxYear,
              `X-CorrelationId` = header("X-CorrelationId", result).get,
              response = AuditResponse(
                httpStatus = expectedStatus,
                errors = Some(Seq(AuditError(mtdError.code)))
              )
            )

            val event: AuditEvent[GenericAuditDetail] = AuditEvent[GenericAuditDetail](
              auditType = "sampleAuditType",
              transactionName = "sample-transaction-type",
              detail = detail
            )

            MockAuditService.verifyAuditEvent(event).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
