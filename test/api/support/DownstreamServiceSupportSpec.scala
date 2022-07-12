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

package api.support

import api.downstream.common.connectors.DownstreamOutcome
import api.endpoints.sample.retrieve.v1.response.RetrieveSampleResponse
import api.models.ResponseWrapper
import api.models.errors._
import cats.syntax.either._
import support.UnitSpec
import utils.Logging

class DownstreamServiceSupportSpec extends UnitSpec with DownstreamServiceSupport with Logging {

  type D = String
  type V = String

  private val ep            = "someEndpoint"
  private val correlationId = "correllationId"

  private val downstreamError1        = MtdError("DOWNSTREAM_CODE1", "downstreammsg1")
  private val downstreamError2        = MtdError("DOWNSTREAM_CODE2", "downstreammsg2")
  private val downstreamError3        = MtdError("DOWNSTREAM_CODE_DOWNSTREAM", "downstreammsg3")
  private val downstreamErrorUnmapped = MtdError("DOWNSTREAM_UNMAPPED", "downstreammsg4")

  private val error1 = MtdError("CODE1", "msg1")
  private val error2 = MtdError("CODE2", "msg2")

  private val downstreamToMtdErrorMap: PartialFunction[String, MtdError] = {
    case "DOWNSTREAM_CODE1"           => error1
    case "DOWNSTREAM_CODE2"           => error2
    case "DOWNSTREAM_CODE_DOWNSTREAM" => StandardDownstreamError
  }

  private val mapToError: ResponseWrapper[D] => Either[ErrorWrapper, ResponseWrapper[D]] = { _: ResponseWrapper[D] =>
    ErrorWrapper(Some(correlationId), error1, None).asLeft[ResponseWrapper[V]]
  }

  "mapToVendor" when {
    val mapToUpperCase: ResponseWrapper[D] => Either[ErrorWrapper, ResponseWrapper[D]] = { downstreamResponse: ResponseWrapper[D] =>
      Right(ResponseWrapper(downstreamResponse.correlationId, downstreamResponse.responseData.toUpperCase))
    }

    "downstream returns a success outcome" when {
      val goodResponse = ResponseWrapper(correlationId, "downstreamResponse").asRight

      "the specified mapping function returns success" must {
        "use that as the success result" in {
          mapToVendor(ep, downstreamToMtdErrorMap)(mapToUpperCase)(goodResponse) shouldBe
            ResponseWrapper(correlationId, "DOWNSTREAMRESPONSE").asRight
        }
      }

      "the specified mapping function returns a failure" must {
        "use that as the failure result" in {
          mapToVendor(ep, downstreamToMtdErrorMap)(mapToError)(goodResponse) shouldBe
            ErrorWrapper(Some(correlationId), error1, None).asLeft
        }
      }
    }

    "downstream returns an error" when {
      singleErrorBehaveCorrectly(mapToVendor(ep, downstreamToMtdErrorMap)(mapToUpperCase))

      multipleErrorsBehaveCorrectly(mapToVendor(ep, downstreamToMtdErrorMap)(mapToUpperCase))
    }
  }

  "mapToVendorDirect" when {
    "downstream returns a success outcome" when {
      val goodResponse = ResponseWrapper(correlationId, "downstreamResponse").asRight

      "use the downstream content as is" must {
        "use that as the success result" in {
          mapToVendorDirect(ep, downstreamToMtdErrorMap)(goodResponse) shouldBe
            ResponseWrapper(correlationId, "downstreamResponse").asRight
        }
      }
    }

    "downstream returns an error" when {
      singleErrorBehaveCorrectly(mapToVendorDirect(ep, downstreamToMtdErrorMap))

      multipleErrorsBehaveCorrectly(mapToVendorDirect(ep, downstreamToMtdErrorMap))
    }
  }

  private def singleDownstreamError(err: MtdError): DownstreamErrors =
    DownstreamErrors.single(DownstreamErrorCode(err.code))

  private def multipleDownstreamErrors(errs: Seq[MtdError]): DownstreamErrors =
    DownstreamErrors(errs.map(err => DownstreamErrorCode(err.code)))

  private def singleErrorBehaveCorrectly(handler: DownstreamOutcome[D] => VendorOutcome[D]): Unit = {
    "a single error" must {
      "use the error mapping and return a single mtd error" in {
        val singleErrorResponse = ResponseWrapper(correlationId, singleDownstreamError(downstreamError1)).asLeft

        handler(singleErrorResponse) shouldBe
          ErrorWrapper(Some(correlationId), error1, None).asLeft
      }
    }

    "a single unmapped error" must {
      "map to a DownstreamError" in {
        val singleErrorResponse = ResponseWrapper(correlationId, singleDownstreamError(downstreamErrorUnmapped)).asLeft

        handler(singleErrorResponse) shouldBe
          ErrorWrapper(Some(correlationId), StandardDownstreamError, None).asLeft
      }
    }

    "an OutboundError" must {
      "return the error inside the OutboundError (regardless of mapping)" in {
        val outboundErrorResponse = ResponseWrapper(correlationId, OutboundError(downstreamError1)).asLeft

        handler(outboundErrorResponse) shouldBe
          ErrorWrapper(Some(correlationId), downstreamError1, None).asLeft
      }
    }
  }

  private def multipleErrorsBehaveCorrectly(handler: DownstreamOutcome[D] => VendorOutcome[D]): Unit = {
    "multiple errors" must {
      "use the error mapping for each and return multiple mtd errors" in {
        val multipleErrorResponse = ResponseWrapper(correlationId, multipleDownstreamErrors(List(downstreamError1, downstreamError2))).asLeft

        handler(multipleErrorResponse) shouldBe
          ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(error1, error2))).asLeft
      }

      "one of the mtd errors is a DownstreamError" must {
        "return a single DownstreamError" in {
          val multipleErrorResponse = ResponseWrapper(correlationId, multipleDownstreamErrors(List(downstreamError1, downstreamError3))).asLeft

          handler(multipleErrorResponse) shouldBe
            ErrorWrapper(Some(correlationId), StandardDownstreamError, None).asLeft
        }
      }

      "one of the mtd errors is a unmapped" must {
        "return a single DownstreamError" in {
          val multipleErrorResponse = ResponseWrapper(correlationId, multipleDownstreamErrors(List(downstreamError1, downstreamErrorUnmapped))).asLeft

          handler(multipleErrorResponse) shouldBe
            ErrorWrapper(Some(correlationId), StandardDownstreamError, None).asLeft
        }
      }
    }
  }

  "validateRetrieveResponse" when {
    "passed an empty response" should {
      "return a NotFoundError error" in {
        validateRetrieveResponse(ResponseWrapper(correlationId, RetrieveSampleResponse(None, None, None, None))) shouldBe
          Left(ErrorWrapper(Some(correlationId), NotFoundError))
      }
    }
    "passed anything else" should {
      "pass it through" in {
        validateRetrieveResponse(ResponseWrapper(correlationId, NotFoundError)) shouldBe
          Right(ResponseWrapper(correlationId, NotFoundError))
      }
    }
  }

}
