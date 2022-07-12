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

import api.controllers.EndpointLogContext
import api.downstream.sample.connectors.MockSampleConnector
import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.models.ResponseWrapper
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.services.ServiceSpec

import scala.concurrent.Future

class DeleteSampleServiceSpec extends ServiceSpec {

  private val nino          = "AA123456A"
  private val taxYear       = "2017-18"
  private val correlationId = "X-123"

  private val requestData = DeleteSampleRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  trait Test extends MockSampleConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteSampleService(
      connector = mockSampleConnector
    )

  }

  "DeleteSampleService" when {
    "delete is called" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockSampleConnector
          .deleteSample(requestData)
          .returns(Future.successful(outcome))

        await(service.delete(requestData)) shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockSampleConnector
            .deleteSample(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.delete(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
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
