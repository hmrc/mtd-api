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

package definition

import config.AppConfig
import utils.Logging
import javax.inject.{Inject, Singleton}
import routing.{Version, Version1}
import routing.testthing.Version1

@Singleton
class ApiDefinitionFactory @Inject() (appConfig: AppConfig) extends Logging {

  private val readScope  = "read:self-assessment"
  private val writeScope = "write:self-assessment"

  lazy val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "View your Self Assessment information",
          description = "Allow read access to self assessment data"
        ),
        Scope(
          key = writeScope,
          name = "Change your Self Assessment information",
          description = "Allow write access to self assessment data"
        )
      ),
      api = APIDefinition(
        name = "#mtd-api# (MTD)",
        description = "#desc#",
        context = appConfig.apiGatewayContext,
        categories = Seq("INCOME_TAX_MTD"),
        versions = Seq(
          APIVersion(
            version = Version1,
            status = buildAPIStatus(Version1),
            endpointsEnabled = appConfig.endpointsEnabled(Version1.configName)
          )
        ),
        requiresTrust = None
      )
    )

  private[definition] def buildAPIStatus(version: Version): APIStatus = {
    APIStatus.parser
      .lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
      }
  }

}
