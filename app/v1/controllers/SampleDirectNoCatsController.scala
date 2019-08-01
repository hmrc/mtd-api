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

package v1.controllers

import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers.SampleRequestDataParser
import v1.models.audit.{AuditEvent, SampleAuditDetail, SampleAuditResponse}
import v1.models.auth.UserDetails
import v1.models.domain.SampleResponse
import v1.models.errors._
import v1.models.requestData.SampleRawData
import v1.orchestrators.DesResponseMappingSupport
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SampleDirectNoCatsController @Inject()(val authService: EnrolmentsAuthService,
                                             val lookupService: MtdIdLookupService,
                                             requestDataParser: SampleRequestDataParser,
                                             sampleService: SampleService,
                                             auditService: AuditService,
                                             cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with DesResponseMappingSupport with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "SampleController", endpointName = "sampleEndpoint")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = SampleRawData(nino, taxYear, request.body)

      requestDataParser.parseRequest(rawData) match {
        case Right(parsedRequest) => sampleService.doService(parsedRequest).map {
          case Right(desResponse) =>
            logger.info(s"${endpointLogContext.controllerName}][${endpointLogContext.endpointName} Success response received with correlationId: ${desResponse.correlationId}")
            auditSubmission(createAuditDetails(rawData, CREATED, desResponse.correlationId, request.userDetails))

            Created(Json.toJson(SampleResponse(desResponse.responseData.responseData)))
              .withApiHeaders(desResponse.correlationId).as(MimeTypes.JSON)

          case Left(desErrorWrapper) =>
            val mtdErrorWrapper = mapDesErrors(desErrorMap)(desErrorWrapper)
            val correlationId = getCorrelationId(mtdErrorWrapper)
            val result = errorResult(mtdErrorWrapper).withApiHeaders(correlationId)
            auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(mtdErrorWrapper)))
            result
        }

        case Left(mtdErrorWrapper) =>
          val correlationId = getCorrelationId(mtdErrorWrapper)
          val result = errorResult(mtdErrorWrapper).withApiHeaders(correlationId)
          auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(mtdErrorWrapper)))
          Future.successful(result)
      }
    }

  private def desErrorMap =
    Map(
      "NOT_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case RuleIncorrectOrEmptyBodyError | BadRequestError | NinoFormatError | TaxYearFormatError | RuleTaxYearNotSupportedError |
           RuleTaxYearRangeExceededError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: SampleRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None): SampleAuditDetail = {
    val response = errorWrapper
      .map {
        wrapper =>
          SampleAuditResponse(statusCode, Some(wrapper.auditErrors))
      }
      .getOrElse(SampleAuditResponse(statusCode, None))

    SampleAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, rawData.taxYear, correlationId, response)
  }

  private def auditSubmission(details: SampleAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("sampleAuditType", "sample-transaction-type", details)
    auditService.auditEvent(event)
  }
}
