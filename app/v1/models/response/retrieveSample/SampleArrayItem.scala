/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveSample

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._
import utils.JsonUtils
import v1.models.domain.DesTaxYear

case class SampleArrayItem(id: String, itemType: Option[SampleEnum], value: Option[BigDecimal], taxYear: String, finalised: Boolean)

object SampleArrayItem extends JsonUtils {

  implicit val reads: Reads[SampleArrayItem] = (
    (JsPath \ "itemId").read[String] and
      (JsPath \ "typeOfItem").readNullable[SampleEnum] and
      (JsPath \ "itemDetail" \ "taxableAmount").readNestedNullable[BigDecimal] and
      (JsPath \ "taxYear").read[String].map(DesTaxYear.fromDes(_).value) and
      (JsPath \ "isFinalised").readWithDefault(defaultValue = false)[Boolean]
    ) (SampleArrayItem.apply _)

  implicit val writes: OWrites[SampleArrayItem] = Json.writes[SampleArrayItem]
}
