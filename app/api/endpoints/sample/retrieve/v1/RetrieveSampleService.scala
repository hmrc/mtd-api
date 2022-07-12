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

import api.downstream.sample.SampleOutcomes.RetrieveSampleOutcome
import api.downstream.sample.connectors.SampleConnector
import api.endpoints.sample.retrieve.v1.request.RetrieveSampleRequest
import api.endpoints.sample.retrieve.v1.response.RetrieveSampleResponse
import api.models.errors._
import api.support.DownstreamServiceSupport
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveSampleService @Inject() (connector: SampleConnector) extends DownstreamServiceSupport with Logging {

  def retrieve(request: RetrieveSampleRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RetrieveSampleOutcome] = {
    connector
      .retrieveSample(request)
      .map(mapToVendorDirect("retrieveSample", errorMap))
      .map { downstreamOutcome =>
        downstreamOutcome.flatMap { responseWrapper =>
          validateRetrieveResponse[RetrieveSampleResponse](responseWrapper)
        }
      }
  }

  private def errorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"        -> NinoFormatError,
    "INVALID_TAX_YEAR"    -> TaxYearFormatError,
    "NOT_FOUND"           -> NotFoundError,
    "SERVER_ERROR"        -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE" -> StandardDownstreamError
  )

}
