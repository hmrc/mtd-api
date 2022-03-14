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

package api.models.domain

import api.models.domain.SampleDownstreamEnum._
import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class SampleDownstreamEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  val downstreamJson: JsValue = Json.toJson("")

  testRoundTrip[SampleDownstreamEnum](
    ("Type1", Type1),
    ("Type2", Type2),
    ("Type3", Type3),
    ("Type4", Type4)
  )

  "SampleDownstreamEnum" when {
    "given an invalid field" should {
      "return a JsError" in {
        downstreamJson.validate[SampleDownstreamEnum] shouldBe a[JsError]
      }
    }

    "toMtdEnum" should {
      "produce the correct SampleMtdEnum object" in {
        Type1.toMtdEnum shouldBe SampleMtdEnum.One
        Type2.toMtdEnum shouldBe SampleMtdEnum.Two
        Type3.toMtdEnum shouldBe SampleMtdEnum.Three
        Type4.toMtdEnum shouldBe SampleMtdEnum.Four
      }
    }
  }
}
