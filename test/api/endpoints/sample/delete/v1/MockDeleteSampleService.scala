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

package api.endpoints.sample.delete.v1

import api.endpoints.sample.delete.v1.request.DeleteSampleRequest
import api.models.ResponseWrapper
import api.models.errors.ErrorWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteSampleService extends MockFactory {

  val mockDeleteSampleService: DeleteSampleService = mock[DeleteSampleService]

  object MockDeleteSampleService {

    def delete(requestData: DeleteSampleRequest): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] =
      (mockDeleteSampleService
        .delete(_: DeleteSampleRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(requestData, *, *)

  }

}
