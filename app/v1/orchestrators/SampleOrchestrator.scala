/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.orchestrators

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.domain.SampleResponse
import v1.models.errors.{DownstreamError, ErrorWrapper, NinoFormatError, NotFoundError, TaxYearFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.SampleRequestData
import v1.services.SampleService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SampleOrchestrator @Inject()(sampleService: SampleService) extends DesResponseMappingSupport with Logging {

  def orchestrate(request: SampleRequestData)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[Either[ErrorWrapper, ResponseWrapper[SampleResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(sampleService.doService(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper.map(des => SampleResponse(des.responseData)) // *If* need to convert to Mtd

    result.value
  }

  private def desErrorMap =
    Map(
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
