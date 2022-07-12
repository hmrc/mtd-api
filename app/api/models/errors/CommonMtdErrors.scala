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

package api.models.errors

// Format Errors

object NinoFormatError    extends MtdError(code = "FORMAT_NINO", message = "The provided NINO is invalid")
object TaxYearFormatError extends MtdError(code = "FORMAT_TAX_YEAR", message = "The provided tax year is invalid")

// Standard Errors
object NotFoundError           extends MtdError(code = "MATCHING_RESOURCE_NOT_FOUND", message = "Matching resource not found")
object StandardDownstreamError extends MtdError(code = "INTERNAL_SERVER_ERROR", message = "An internal server error occurred")
object BadRequestError         extends MtdError(code = "INVALID_REQUEST", message = "Invalid request")
object BVRError                extends MtdError(code = "BUSINESS_ERROR", message = "Business validation error")
object ServiceUnavailableError extends MtdError(code = "SERVICE_UNAVAILABLE", message = "Internal server error")

// Authorisation Errors
object UnauthorisedError       extends MtdError(code = "CLIENT_OR_AGENT_NOT_AUTHORISED", message = "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError(code = "UNAUTHORIZED", message = "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError(code = "ACCEPT_HEADER_INVALID", message = "The accept header is missing or invalid")
object UnsupportedVersionError  extends MtdError(code = "NOT_FOUND", message = "The requested resource could not be found")
object InvalidBodyTypeError     extends MtdError(code = "INVALID_BODY_TYPE", message = "Expecting text/json or application/json body")
