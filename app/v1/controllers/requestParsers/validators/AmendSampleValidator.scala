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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.models.errors.MtdError
import api.models.request.amendSample.{ AmendSampleRawData, AmendSampleRequestBody }
import api.validations.{ JsonFormatValidation, NinoValidation, TaxYearNotSupportedValidation, TaxYearValidation }
import config.AppConfig

import javax.inject.Inject

class AmendSampleValidator @Inject()(implicit appConfig: AppConfig) extends Validator[AmendSampleRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: AmendSampleRawData => List[List[MtdError]] = (data: AmendSampleRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      JsonFormatValidation.validate[AmendSampleRequestBody](data.body)
    )
  }

  private def parameterRuleValidation: AmendSampleRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

  override def validate(data: AmendSampleRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
