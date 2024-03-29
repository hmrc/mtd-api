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

package api.endpoints.sample.domain.v1

import api.endpoints.sample.domain.v1.SampleMtdEnum.{Four, One, Three, Two}
import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class SampleMtdEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  val downstreamJson: JsValue = Json.toJson("")

  testRoundTrip[SampleMtdEnum](
    ("One", One),
    ("Two", Two),
    ("Three", Three),
    ("Four", Four)
  )

  "SampleMtdEnum" when {
    "given an invalid field" should {
      "return a JsError" in {
        downstreamJson.validate[SampleMtdEnum] shouldBe a[JsError]
      }
    }

    "toDownstreamEnum" should {
      "produce the correct SampleMtdEnum object" in {
        One.toDownstreamEnum shouldBe SampleDownstreamEnum.Type1
        Two.toDownstreamEnum shouldBe SampleDownstreamEnum.Type2
        Three.toDownstreamEnum shouldBe SampleDownstreamEnum.Type3
        Four.toDownstreamEnum shouldBe SampleDownstreamEnum.Type4
      }
    }
  }

}
