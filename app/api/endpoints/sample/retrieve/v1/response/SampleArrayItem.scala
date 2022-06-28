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

import api.endpoints.sample.domain.v1.{SampleDownstreamEnum, SampleMtdEnum}
import api.models.domain.TaxYear
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils

case class SampleArrayItem(id: String,
                           declaredAmount: Option[BigDecimal],
                           taxableAmount: Option[BigDecimal],
                           itemType: SampleMtdEnum,
                           taxYear: String,
                           finalised: Boolean)

object SampleArrayItem extends JsonUtils {

  implicit val reads: Reads[SampleArrayItem] = (
    (JsPath \ "itemId").read[String] and
      (JsPath \ "submittedAmount").readNullable[BigDecimal] and
      (JsPath \ "itemDetail" \ "taxableAmount").readNestedNullable[BigDecimal] and
      (JsPath \ "typeOfItem").read[SampleDownstreamEnum].map(_.toMtdEnum) and
      (JsPath \ "taxYear").read[String].map(TaxYear.fromDownstream(_).toMtd) and
      (JsPath \ "isFinalised").readWithDefault(defaultValue = false)
  )(SampleArrayItem.apply _)

  implicit val writes: OWrites[SampleArrayItem] = Json.writes[SampleArrayItem]
}
