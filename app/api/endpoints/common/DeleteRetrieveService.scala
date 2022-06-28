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

package api.endpoints.common

import api.connectors.{DeleteRetrieveConnector, DownstreamUri}
import api.controllers.EndpointLogContext
import api.models.ResponseWrapper
import api.models.errors._
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteRetrieveService @Inject() (connector: DeleteRetrieveConnector) extends DownstreamResponseMappingSupport with Logging {

  def delete(downstreamErrorMap: Map[String, MtdError] = errorMap)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      downstreamUri: DownstreamUri[Unit]): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.delete()).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield downstreamResponseWrapper

    result.value
  }

  def retrieve[Resp: Format](downstreamErrorMap: Map[String, MtdError] = errorMap)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      downstreamUri: DownstreamUri[Resp]): Future[Either[ErrorWrapper, ResponseWrapper[Resp]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieve[Resp]()).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](validateRetrieveResponse(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def errorMap: Map[String, MtdError] = Map(
    "INVALID_NINO"        -> NinoFormatError,
    "INVALID_TAX_YEAR"    -> TaxYearFormatError,
    "NOT_FOUND"           -> NotFoundError,
    "SERVER_ERROR"        -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE" -> StandardDownstreamError
  )

}
