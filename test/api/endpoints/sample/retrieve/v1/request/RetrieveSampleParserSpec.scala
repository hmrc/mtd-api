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

package api.endpoints.sample.retrieve.v1.request

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec

class RetrieveSampleParserSpec extends UnitSpec {

  val nino: String    = "AA123456B"
  val taxYear: String = "2017-18"

  val retrieveSampleRawData: RetrieveSampleRawData = RetrieveSampleRawData(
    nino = nino,
    taxYear = taxYear
  )

  trait Test extends MockRetrieveSampleValidator {

    lazy val parser: RetrieveSampleParser = new RetrieveSampleParser(
      validator = mockRetrieveSampleValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveSampleValidator.validate(retrieveSampleRawData).returns(Nil)

        parser.parseRequest(retrieveSampleRawData) shouldBe
          Right(RetrieveSampleRequest(Nino(nino), TaxYear.fromDownstream("2018")))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveSampleValidator
          .validate(retrieveSampleRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(retrieveSampleRawData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveSampleValidator
          .validate(retrieveSampleRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(retrieveSampleRawData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
