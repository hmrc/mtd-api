/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.mocks.orchestrators

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.domain.SampleResponse
import v1.models.errors.ErrorWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.SampleRequestData
import v1.orchestrators.SampleOrchestrator

import scala.concurrent.{ExecutionContext, Future}

trait MockSampleOrchestrator extends MockFactory {

  val mockSampleOrchestrator: SampleOrchestrator = mock[SampleOrchestrator]

  object MockSampleOrchestrator {

    def orchestrate(requestData: SampleRequestData): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[SampleResponse]]]] = {
      (mockSampleOrchestrator
        .orchestrate(_: SampleRequestData)(_: HeaderCarrier, _: ExecutionContext, _: EndpointLogContext))
        .expects(requestData, *, *, *)
    }
  }

}