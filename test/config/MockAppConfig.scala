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

package config

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockAppConfig {
    // MTD ID Lookup Config
    def mtdIdBaseUrl: CallHandler[String] = (() => mockAppConfig.mtdIdBaseUrl: String).expects()

    // DES Config
    def desBaseUrl: CallHandler[String]                         = (() => mockAppConfig.desBaseUrl: String).expects()
    def desToken: CallHandler[String]                           = (() => mockAppConfig.desToken: String).expects()
    def desEnvironment: CallHandler[String]                     = (() => mockAppConfig.desEnv: String).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders: Option[Seq[String]]).expects()

    // IFS Config
    def ifsBaseUrl: CallHandler[String]                         = (() => mockAppConfig.ifsBaseUrl: String).expects()
    def ifsToken: CallHandler[String]                           = (() => mockAppConfig.ifsToken: String).expects()
    def ifsEnvironment: CallHandler[String]                     = (() => mockAppConfig.ifsEnv: String).expects()
    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.ifsEnvironmentHeaders: Option[Seq[String]]).expects()

    // Business Rule Config
    def minimumPermittedTaxYear: CallHandler[Int] = (() => mockAppConfig.minimumPermittedTaxYear: Int).expects()

    // API Config
    def featureSwitch: CallHandler[Option[Configuration]]        = (() => mockAppConfig.featureSwitch: Option[Configuration]).expects()
    def apiGatewayContext: CallHandler[String]                   = (() => mockAppConfig.apiGatewayContext: String).expects()
    def apiStatus(version: Version): CallHandler[String]         = (mockAppConfig.apiStatus: Version => String).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled: String => Boolean).expects(version.name)

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] = (() => mockAppConfig.confidenceLevelConfig: ConfidenceLevelConfig).expects()

  }

}
