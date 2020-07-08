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

// This is based on the 'Retrieve Dividends Income' endpoint on the individuals-income-received-api
// https://github.com/hmrc/individuals-income-received-api
// https://confluence.tools.tax.service.gov.uk/display/MTE/Retrieve+dividends+income+-+Technical+Specs

case class RetrieveSampleResponse(foreignDividend: Option[Seq[ForeignDividendItem]],
                                  dividendIncomeReceivedWhilstAbroad: Option[Seq[DividendIncomeReceivedWhilstAbroadItem]],
                                  stockDividend: Option[StockDividend],
                                  redeemableShares: Option[RedeemableShares],
                                  bonusIssuesOfSecurities: Option[BonusIssuesOfSecurities],
                                  closeCompanyLoansWrittenOff: Option[CloseCompanyLoansWrittenOff])

object RetrieveSampleResponse extends HateoasLinks with JsonUtils {

  val empty: RetrieveSampleResponse = RetrieveSampleResponse(None, None, None, None, None, None)

  implicit val reads: Reads[RetrieveSampleResponse] = (
    (JsPath \ "foreignDividend").readNullable[Seq[ForeignDividendItem]].mapEmptySeqToNone and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").readNullable[Seq[DividendIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "stockDividend").readNullable[StockDividend] and
      (JsPath \ "redeemableShares").readNullable[RedeemableShares] and
      (JsPath \ "bonusIssuesOfSecurities").readNullable[BonusIssuesOfSecurities] and
      (JsPath \ "closeCompanyLoansWrittenOff").readNullable[CloseCompanyLoansWrittenOff]
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
