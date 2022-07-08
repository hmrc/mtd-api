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

package api.endpoints.sample.delete.v1

import api.controllers.ControllerBaseSpec
import api.endpoints.sample.delete.v1.request.{DeleteSampleRawData, DeleteSampleRequest, MockDeleteSampleRequestParser}
import api.models.ResponseWrapper
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.errors.v1.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError}
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteSampleControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteSampleService
    with MockDeleteSampleRequestParser {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2017-18"
  val correlationId: String = "X-123"

  val rawData: DeleteSampleRawData = DeleteSampleRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: DeleteSampleRequest = DeleteSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = TaxYear.fromMtd(taxYear)
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteSampleController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteSampleRequestParser,
      service = mockDeleteSampleService,
      cc = cc
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockEnrolmentsAuthService.authoriseUser()
  }

  "DeleteSampleController" should {
    "return NO_CONTENT" when {
      "happy path" in new Test {

        MockDeleteSampleRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteSampleService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.deleteSample(nino, taxYear)(fakeDeleteRequest)

        status(result) shouldBe NO_CONTENT
        contentAsString(result) shouldBe ""
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteSampleRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.deleteSample(nino, taxYear)(fakeDeleteRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteSampleRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteSampleService
              .delete(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.deleteSample(nino, taxYear)(fakeDeleteRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
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
