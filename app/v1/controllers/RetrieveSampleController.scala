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

package v1.controllers

import api.connectors.DownstreamUri
import api.connectors.DownstreamUri.IfsUri
import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.hateoas.HateoasFactory
import api.models.domain.DownstreamTaxYear
import api.models.errors._
import api.models.request.DeleteRetrieveRawData
import api.requestParsers.DeleteRetrieveRequestParser
import api.services.{DeleteRetrieveService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.Logging
import v1.models.response.retrieveSample.{RetrieveSampleHateoasData, RetrieveSampleResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveSampleController @Inject()(val authService: EnrolmentsAuthService,
                                         val lookupService: MtdIdLookupService,
                                         requestParser: DeleteRetrieveRequestParser,
                                         service: DeleteRetrieveService,
                                         hateoasFactory: HateoasFactory,
                                         cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "RetrieveSampleController",
    endpointName = "retrieveSample"
  )

  def retrieveSample(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      val rawData: DeleteRetrieveRawData = DeleteRetrieveRawData(
        nino = nino,
        taxYear = taxYear
      )

      implicit val downstreamUri: DownstreamUri[RetrieveSampleResponse] = IfsUri[RetrieveSampleResponse](
        s"sample/$nino/${DownstreamTaxYear.fromMtd(taxYear)}"
      )

      val result =
        for {
          _               <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieve[RetrieveSampleResponse]())
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData, RetrieveSampleHateoasData(nino, taxYear))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result        = errorResult(errorWrapper).withApiHeaders(correlationId)

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TaxYearFormatError |
           RuleTaxYearRangeInvalidError | RuleTaxYearNotSupportedError
      => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case StandardDownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _ => unhandledError(errorWrapper)
    }
  }
}
