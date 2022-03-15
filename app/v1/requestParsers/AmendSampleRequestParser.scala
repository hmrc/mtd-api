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

package v1.requestParsers

import api.models.domain.{DownstreamTaxYear, Nino}
import api.models.request.amendSample.{AmendSampleRawData, AmendSampleRequest, AmendSampleRequestBody}
import api.requestParsers.RequestParser
import v1.requestParsers.validators.AmendSampleValidator

import javax.inject.Inject

class AmendSampleRequestParser @Inject()(val validator: AmendSampleValidator) extends RequestParser[AmendSampleRawData, AmendSampleRequest] {

  override protected def requestFor(data: AmendSampleRawData): AmendSampleRequest =
    AmendSampleRequest(Nino(data.nino), DownstreamTaxYear.fromMtd(data.taxYear), data.body.as[AmendSampleRequestBody])
}