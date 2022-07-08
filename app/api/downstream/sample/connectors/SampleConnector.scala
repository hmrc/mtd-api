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

package api.downstream.sample.connectors

import api.downstream.common.connectors.DownstreamUri.IfsUri
import api.downstream.common.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.endpoints.sample.amend.v1.request.AmendSampleRequest
import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.endpoints.sample.retrieve.v1.request.RetrieveSampleRequest
import api.endpoints.sample.retrieve.v1.response.RetrieveSampleResponse
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SampleConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def amendSample(request: AmendSampleRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DownstreamOutcome[Unit]] = {

    import api.downstream.common.httpparsers.StandardDownstreamHttpParser._

    val nino    = request.nino.nino
    val taxYear = request.downstreamTaxYear

    put(
      body = request.body,
      IfsUri[Unit](s"some-placeholder/template/$nino/${taxYear.toDownstream}")
    )
  }

  def retrieveSample(
      request: RetrieveSampleRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DownstreamOutcome[RetrieveSampleResponse]] = {

    import api.downstream.common.httpparsers.StandardDownstreamHttpParser._

    val nino    = request.nino.nino
    val taxYear = request.downstreamTaxYear

    get(
      IfsUri[RetrieveSampleResponse](s"some-placeholder/template/$nino/${taxYear.toDownstream}")
    )
  }

  def deleteSample(request: DeleteSampleRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DownstreamOutcome[Unit]] = {
    import api.downstream.common.httpparsers.StandardDownstreamHttpParser._

    val nino    = request.nino.nino
    val taxYear = request.downstreamTaxYear

    delete(uri = IfsUri[Unit](s"some-placeholder/template/$nino/${taxYear.toDownstream}"))
  }

}
