package api.endpoints.sample.amend.v1.request

import api.models.errors.ErrorWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory

trait MockAmendSampleRequestParser extends MockFactory {

  val mockAmendSampleRequestParser: AmendSampleRequestParser = mock[AmendSampleRequestParser]

  object MockAmendSampleRequestParser {

    def parse(data: AmendSampleRawData): CallHandler[Either[ErrorWrapper, AmendSampleRequest]] = {
      (mockAmendSampleRequestParser.parseRequest(_: AmendSampleRawData)).expects(data)
    }

  }

}
