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

package api.endpoints.sample.retrieve.v1.response

import api.endpoints.sample.domain.v1.SampleMtdEnum
import api.models.utils.JsonErrorValidators
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class SampleArrayItemSpec extends UnitSpec with JsonErrorValidators {

  val model: SampleArrayItem = SampleArrayItem(
    id = "AAA123",
    declaredAmount = Some(200.11),
    taxableAmount = Some(100.14),
    itemType = SampleMtdEnum.One,
    taxYear = "2018-19",
    finalised = true
  )

  "SampleArrayItem" when {
    "read from valid JSON" should {
      "produce the expected SampleArrayItem object" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "itemId": "AAA123",
            |  "submittedAmount": 200.11,
            |  "itemDetail": {
            |    "taxableAmount": 100.14
            |  },
            |  "typeOfItem": "Type1",
            |  "taxYear": "2019",
            |  "isFinalised": true
            |}
          """.stripMargin
        )

        json.as[SampleArrayItem] shouldBe model
      }
    }

    "read from valid JSON with missing optional fields" should {
      "produce the expected SampleArrayItem object" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "itemId": "AAA123",
            |  "itemDetail": {
            |  },
            |  "typeOfItem": "Type1",
            |  "taxYear": "2019"
            |}
          """.stripMargin
        )

        json.as[SampleArrayItem] shouldBe model.copy(
          declaredAmount = None,
          taxableAmount = None,
          finalised = false
        )
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "id": "AAA123",
            |  "declaredAmount": 200.11,
            |  "taxableAmount": 100.14,
            |  "itemType": "One",
            |  "taxYear": "2018-19",
            |  "finalised": true
            |}
          """.stripMargin
        )

        Json.toJson(model) shouldBe json
      }
    }

    "written to JSON with missing optional fields" should {
      "produce the expected JsObject" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "id": "AAA123",
            |  "itemType": "One",
            |  "taxYear": "2018-19",
            |  "finalised": true
            |}
          """.stripMargin
        )

        Json.toJson(model.copy(declaredAmount = None, taxableAmount = None)) shouldBe json
      }
    }
  }

}
