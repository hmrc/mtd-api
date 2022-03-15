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

package api.services

import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.models.request.amendSample.AmendSampleRequest
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.AmendSampleConnector

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AmendSampleService @Inject()(connector: AmendSampleConnector) extends DownstreamResponseMappingSupport with Logging {

  def amendSample(request: AmendSampleRequest)(implicit hc: HeaderCarrier,
                                               ec: ExecutionContext,
                                               logContext: EndpointLogContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.amendSample(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield downstreamResponseWrapper.map(des => des) // For any additional mapping

    result.value
  }

  private def desErrorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"        -> NinoFormatError,
    "INVALID_TAX_YEAR"    -> TaxYearFormatError,
    "NOT_FOUND"           -> NotFoundError,
    "SERVER_ERROR"        -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE" -> StandardDownstreamError
  )
}