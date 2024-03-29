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

package api.endpoints.sample.delete.v1.request

import api.models.errors._
import api.models.errors.v1.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError}
import config.MockAppConfig
import org.joda.time.format.DateTimeFormat
import support.UnitSpec

class DeleteSampleValidatorSpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  class Test extends MockAppConfig {
    implicit val appConfig = mockAppConfig
    val dateTimeFormatter  = DateTimeFormat.forPattern("yyyy-MM-dd")
    val validator          = new DeleteSampleValidator()

    MockAppConfig.minimumPermittedTaxYear
      .returns(2021)

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(DeleteSampleRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(DeleteSampleRawData("A12344A", validTaxYear)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteSampleRawData(validNino, "20178")) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteSampleRawData(validNino, "2017-19")) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteSampleRawData(validNino, "2016-17")) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(DeleteSampleRawData("A12344A", "20178")) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
