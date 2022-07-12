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

import api.models.errors.MtdError
import api.validations.Validator
import api.validations.anyVersion.NinoValidation
import api.validations.v1.{TaxYearNotSupportedValidation, TaxYearValidation}
import config.AppConfig

import javax.inject.Inject

class DeleteSampleValidator @Inject() (implicit appConfig: AppConfig) extends Validator[DeleteSampleRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: DeleteSampleRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: DeleteSampleRawData => List[List[MtdError]] = (data: DeleteSampleRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: DeleteSampleRawData => List[List[MtdError]] = (data: DeleteSampleRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

}
