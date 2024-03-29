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
import api.models.hateoas.Link
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec

class RetrieveSampleResponseSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  val nino: String    = "AA123456A"
  val taxYear: String = "2020-21"

  val arrayItemModel1: SampleArrayItem = SampleArrayItem(
    id = "AAA123",
    declaredAmount = Some(200.11),
    taxableAmount = Some(100.14),
    itemType = SampleMtdEnum.One,
    taxYear = "2018-19",
    finalised = true
  )

  val arrayItemModel2: SampleArrayItem = SampleArrayItem(
    id = "AAA123",
    declaredAmount = Some(200.11),
    taxableAmount = Some(100.14),
    itemType = SampleMtdEnum.One,
    taxYear = "2018-19",
    finalised = true
  )

  val sampleObjectModel: SampleObject = SampleObject(
    dateSubmitted = "01-01-2019",
    submissionItem = Some(arrayItemModel2)
  )

  val sampleOptionalModel1: SampleOptionalObject = SampleOptionalObject(
    itemIdentifier = Some("anId1"),
    itemType = Some(SampleMtdEnum.One),
    deductibleAmount = Some(300.54)
  )

  val sampleOptionalModel2: SampleOptionalObject = SampleOptionalObject(
    itemIdentifier = Some("anId2"),
    itemType = Some(SampleMtdEnum.Four),
    deductibleAmount = Some(400.54)
  )

  val model: RetrieveSampleResponse = RetrieveSampleResponse(
    Some(List(arrayItemModel1)),
    Some(sampleObjectModel),
    Some(sampleOptionalModel1),
    Some(Seq(sampleOptionalModel2))
  )

  "RetrieveSampleResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveSampleResponse object" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "historicalIncomeSubmissions": [
            |    {
            |      "itemId": "AAA123",
            |      "submittedAmount": 200.11,
            |      "itemDetail": {
            |        "taxableAmount": 100.14
            |      },
            |      "typeOfItem": "Type1",
            |      "taxYear": "2019",
            |      "isFinalised": true
            |    }
            |  ],
            |  "currentIncomeSubmission": {
            |    "dateSubmitted": "01-01-2019",
            |    "submittedItems": {
            |      "income": [
            |        {
            |          "itemId": "AAA123",
            |          "submittedAmount": 200.11,
            |          "itemDetail": {
            |            "taxableAmount": 100.14
            |          },
            |          "typeOfItem": "Type1",
            |          "taxYear": "2019",
            |          "isFinalised": true
            |        },
            |        {
            |          "itemId": "AAA123",
            |          "submittedAmount": 200.11,
            |          "itemDetail": {
            |            "taxableAmount": 100.14
            |          },
            |          "typeOfItem": "Type2",
            |          "taxYear": "2019",
            |          "isFinalised": true
            |        }
            |      ]
            |    }
            |  },
            |  "totalCharitableContribution": {
            |    "requestedItemId": "anId1",
            |    "typeOfItem": "Type1",
            |    "paymentAmount": 300.54
            |  },
            |  "broughtForwardSubmissions": [
            |    {
            |      "requestedItemId": "anId2",
            |      "typeOfItem": "Type4",
            |      "paymentAmount": 400.54
            |    },
            |    {
            |      "requestedItemId": "anId",
            |      "typeOfItem": "Type2",
            |      "paymentAmount": 300.54
            |    }
            |  ]
            |}
          """.stripMargin
        )

        json.as[RetrieveSampleResponse] shouldBe model
      }
    }

    "read from empty JSON (no data items)" should {
      "produce an empty RetrieveSampleResponse object" in {
        val json: JsValue = JsObject.empty

        json.as[RetrieveSampleResponse] shouldBe RetrieveSampleResponse.empty
      }
    }

    "read from empty JSON (empty data items)" should {
      "produce an empty RetrieveSampleResponse object" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "historicalIncomeSubmissions": [],
            |  "currentIncomeSubmission": {
            |    "dateSubmitted": "01-01-2019",
            |    "submittedItems": {
            |      "income": [
            |        {
            |          "itemId": "AAA123",
            |          "submittedAmount": 200.11,
            |          "itemDetail": {
            |            "taxableAmount": 100.14
            |          },
            |          "typeOfItem": "Type2",
            |          "taxYear": "2019",
            |          "isFinalised": true
            |        }
            |      ]
            |    }
            |  },
            |  "totalCharitableContribution": {},
            |  "broughtForwardSubmissions": [
            |    {
            |      "requestedItemId": "anId",
            |      "paymentAmount": 300.54
            |    }
            |  ]
            |}
          """.stripMargin
        )

        json.as[RetrieveSampleResponse] shouldBe RetrieveSampleResponse.empty
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        val json: JsValue = Json.parse("""
            |{
            |  "availableCharitableDeduction": {
            |    "deductibleAmount": 300.54,
            |    "itemIdentifier": "anId1",
            |    "itemType": "One"
            |  },
            |  "broughtForwardLosses": [
            |    {
            |      "deductibleAmount": 400.54,
            |      "itemIdentifier": "anId2",
            |      "itemType": "Four"
            |    }
            |  ],
            |  "completedItems": [
            |    {
            |      "declaredAmount": 200.11,
            |      "finalised": true,
            |      "id": "AAA123",
            |      "itemType": "One",
            |      "taxableAmount": 100.14,
            |      "taxYear": "2018-19"
            |    }
            |  ],
            |  "taxableForeignIncome": {
            |    "dateSubmitted": "01-01-2019",
            |    "submissionItem": {
            |        "declaredAmount": 200.11,
            |        "finalised": true,
            |        "id": "AAA123",
            |        "itemType": "One",
            |        "taxableAmount": 100.14,
            |        "taxYear": "2018-19"
            |    }
            |  }
            |}
          """.stripMargin)

        Json.toJson(model) shouldBe json
      }
    }

    "written to JSON (missing mandatory fields)" should {
      "produce the expected JsObject" in {
        val json: JsValue = JsObject.empty

        Json.toJson(RetrieveSampleResponse.empty) shouldBe json
      }
    }
  }

  "Links Factory" should {

    "expose the correct links" in {
      MockAppConfig.apiGatewayContext.returns("mtd/template").anyNumberOfTimes()
      RetrieveSampleResponse.RetrieveSampleLinksFactory.links(mockAppConfig, RetrieveSampleHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/mtd/template/sample/$nino/$taxYear", PUT, "amend-sample-rel"),
          Link(s"/mtd/template/sample/$nino/$taxYear", GET, "self"),
          Link(s"/mtd/template/sample/$nino/$taxYear", DELETE, "delete-sample-rel")
        )
    }
  }

}
