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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{ DownstreamTaxYear, Nino }
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.models.request.amendSample.{ AmendSampleRequest, AmendSampleRequestBody }
import api.services.ServiceSpec
import v1.mocks.connectors.MockAmendSampleConnector

import scala.concurrent.Future

class AmendSampleServiceSpec extends ServiceSpec {

  private val nino          = "AA123456A"
  private val taxYear       = "2017-18"
  private val correlationId = "X-123"

  private val requestBody = AmendSampleRequestBody(
    data = "someData"
  )

  private val requestData = AmendSampleRequest(
    nino = Nino(nino),
    downstreamTaxYear = DownstreamTaxYear.fromMtd(taxYear),
    body = requestBody
  )

  trait Test extends MockAmendSampleConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendSampleService(
      connector = mockAmendSampleConnector
    )
  }

  "AmendSampleService" when {
    "amendSample" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendSampleConnector
          .amendSample(requestData)
          .returns(Future.successful(outcome))

        await(service.amendSample(requestData)) shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockAmendSampleConnector
            .amendSample(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.amendSample(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
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
  }
}
