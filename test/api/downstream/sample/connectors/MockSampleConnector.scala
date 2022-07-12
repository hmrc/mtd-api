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

import api.downstream.common.connectors.DownstreamOutcome
import api.endpoints.sample.amend.v1.request.AmendSampleRequest
import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.endpoints.sample.retrieve.v1.request.RetrieveSampleRequest
import api.endpoints.sample.retrieve.v1.response.RetrieveSampleResponse
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockSampleConnector extends MockFactory {

  val mockSampleConnector: SampleConnector = mock[SampleConnector]

  object MockSampleConnector {

    def amendSample(requestData: AmendSampleRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (mockSampleConnector
        .amendSample(_: AmendSampleRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(requestData, *, *)
    }

    def deleteSample(requestData: DeleteSampleRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (mockSampleConnector
        .deleteSample(_: DeleteSampleRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(requestData, *, *)
    }

    def retrieveSample(requestData: RetrieveSampleRequest): CallHandler[Future[DownstreamOutcome[RetrieveSampleResponse]]] =
      (mockSampleConnector
        .retrieveSample(_: RetrieveSampleRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(requestData, *, *)

  }

}
