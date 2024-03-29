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

package api.endpoints.sample.amend.v1.request

import api.endpoints.sample.amend.v1.request
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import play.api.libs.json.Json
import support.UnitSpec

class AmendSampleRequestParserSpec extends UnitSpec {

  val nino: String    = "AA123456B"
  val taxYear: String = "2017-18"
  val calcId: String  = "someCalcId"

  private val requestBodyJson = Json.parse(
    """{
      |  "data" : "someData"
      |}
    """.stripMargin
  )

  val inputData: AmendSampleRawData = request.AmendSampleRawData(nino, taxYear, requestBodyJson)

  trait Test extends MockAmendSampleValidator {
    lazy val parser = new AmendSampleRequestParser(mockAmendSampleValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendSampleValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendSampleRequest(Nino(nino), TaxYear.fromDownstream("2018"), AmendSampleRequestBody("someData")))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendSampleValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendSampleValidator
          .validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
