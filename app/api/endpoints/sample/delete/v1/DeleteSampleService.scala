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

import api.downstream.sample.SampleOutcomes.DeleteSampleOutcome
import api.downstream.sample.connectors.SampleConnector
import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.models.errors._
import api.support.DownstreamServiceSupport
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteSampleService @Inject() (connector: SampleConnector) extends DownstreamServiceSupport with Logging {

  def delete(request: DeleteSampleRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeleteSampleOutcome] = {
    connector.deleteSample(request).map {
      mapToVendorDirect("deleteSample", errorMap)
    }
  }

  private val errorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"        -> NinoFormatError,
    "INVALID_TAX_YEAR"    -> TaxYearFormatError,
    "NOT_FOUND"           -> NotFoundError,
    "SERVER_ERROR"        -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE" -> StandardDownstreamError
  )

}
