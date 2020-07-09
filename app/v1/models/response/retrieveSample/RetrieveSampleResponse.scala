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

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveSampleResponse(optionalArray: Option[Seq[SampleArrayItem]],
                                  nestedObject: Option[SampleObject],
                                  allOptionalObject: Option[SampleOptionalObject],
                                  filteredArray: Option[Seq[SampleArrayItem]])

object RetrieveSampleResponse extends HateoasLinks with JsonUtils {

  //val empty: RetrieveSampleResponse = RetrieveSampleResponse(None, None, None, None)

  implicit val reads: Reads[RetrieveSampleResponse] = (
    (JsPath \ "historicSubmissions").readNullable[Seq[SampleArrayItem]].mapEmptySeqToNone and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").readNullable[SampleObject] and
      (JsPath \ "stockDividend").readNullable[SampleOptionalObject] and
      (JsPath \ "finalisedSubmissions").readNullable[Seq[SampleArrayItem]]
    ) (RetrieveSampleResponse.apply _)

  implicit val writes: OWrites[RetrieveSampleResponse] = Json.writes[RetrieveSampleResponse]

  implicit object RetrieveSampleLinksFactory extends HateoasLinksFactory[RetrieveSampleResponse, RetrieveSampleHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveSampleHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendSample(appConfig, nino, taxYear),
        retrieveSample(appConfig, nino, taxYear),
        deleteSample(appConfig, nino, taxYear)
      )
    }
  }
}

case class RetrieveSampleHateoasData(nino: String, taxYear: String) extends HateoasData
