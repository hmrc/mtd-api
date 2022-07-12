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

package api.endpoints.sample.retrieve.v1

import api.controllers.EndpointLogContext
import api.downstream.common.connectors.DownstreamOutcome
import api.downstream.common.connectors.DownstreamUri.IfsUri
import api.downstream.sample.SampleOutcomes.RetrieveSampleOutcome
import api.downstream.sample.connectors.MockSampleConnector
import api.endpoints.sample.domain.v1.SampleMtdEnum.One
import api.endpoints.sample.retrieve.v1.request.RetrieveSampleRequest
import api.endpoints.sample.retrieve.v1.response.{RetrieveSampleResponse, SampleArrayItem}
import api.models.ResponseWrapper
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.services.ServiceSpec
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

class RetrieveSampleServiceSpec extends ServiceSpec {

  val nino: String           = "AA112233A"
  val taxYear: String        = "2019"
  val parsedTaxYear: TaxYear = TaxYear.fromMtd(taxYear)
  val correlationId: String  = "X-corr"

  val sampleArrayItem: SampleArrayItem = SampleArrayItem("id", None, None, One, "2021-22", true)

  val request: RetrieveSampleRequest   = RetrieveSampleRequest(Nino(nino), parsedTaxYear)
  val response: RetrieveSampleResponse = RetrieveSampleResponse(Some(List(sampleArrayItem)), None, None, None)

  trait Test extends MockSampleConnector {

    case class Data(field: Option[String])

    object Data {
      implicit val reads: Format[Data] = Json.format[Data]
    }

    implicit val logContext: EndpointLogContext      = EndpointLogContext("c", "ep")
    implicit val deleteDownstreamUri: IfsUri[Unit]   = IfsUri[Unit](s"sample/$nino/$taxYear")
    implicit val retrieveDownstreamUri: IfsUri[Data] = IfsUri[Data](s"sample/$nino/$taxYear")

    val service: RetrieveSampleService = new RetrieveSampleService(connector = mockSampleConnector)

  }

  "RetrieveSampleService" when {
    "retrieve is called" must {
      "return a success result" in new Test {
        val downstreamOutcome: DownstreamOutcome[RetrieveSampleResponse] = Right(ResponseWrapper(correlationId, response))
        val mtdOutcome: RetrieveSampleOutcome                            = Right(ResponseWrapper(correlationId, response))

        MockSampleConnector
          .retrieveSample(request)
          .returns(Future.successful(downstreamOutcome))

        await(service.retrieve(request)) shouldBe mtdOutcome
      }

      "map single-error downstream responses according to spec" when {

        def serviceError(downstreamErrorCode: String, expectedMtdError: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockSampleConnector
              .retrieveSample(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(Some(correlationId), expectedMtdError))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }

      "map a multi-error downstream response" when {

        def serviceErrors(downstreamErrorCode1: String,
                          expectedMtdError1: MtdError,
                          downstreamErrorCode2: String,
                          expectedMtdError2: MtdError): Unit =
          s"$downstreamErrorCode1 and $downstreamErrorCode2 errors are returned from the service" in new Test {

            private val downstreamErrorsToReturn = List(
              DownstreamErrorCode(downstreamErrorCode1),
              DownstreamErrorCode(downstreamErrorCode2)
            )

            private val expectedMtdErrors = List(
              expectedMtdError1,
              expectedMtdError2
            )

            MockSampleConnector
              .retrieveSample(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors(downstreamErrorsToReturn)))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(Some(correlationId), BadRequestError, Some(expectedMtdErrors)))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError, "INVALID_TAX_YEAR", TaxYearFormatError),
          ("NOT_FOUND", NotFoundError, "INVALID_TAX_YEAR", TaxYearFormatError)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
