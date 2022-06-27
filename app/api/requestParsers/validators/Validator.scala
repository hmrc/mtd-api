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

package api.requestParsers.validators

import api.models.errors.MtdError
import api.models.request.RawData

import scala.annotation.tailrec

trait Validator[A <: RawData] {

  type ValidationType = A => List[List[MtdError]]

  def validate(data: A): List[MtdError]

  @tailrec
  final def run(validationSet: List[ValidationType], data: A): List[MtdError] = {
    val nextValidationResultOpt: Option[List[MtdError]] = validationSet.headOption.map(_(data).flatten)

    nextValidationResultOpt match {
      case None                        => List.empty[MtdError]
      case Some(errs) if errs.nonEmpty => errs
      case _                           => run(validationSet.tail, data)
    }
  }

}

object Validator {

  @tailrec
  def flattenErrors(errorsToFlatten: List[List[MtdError]], flatErrors: List[MtdError] = Nil): List[MtdError] = errorsToFlatten.flatten match {
    case Nil         => flatErrors
    case item :: Nil => flatErrors :+ item
    case nextError :: tail =>
      def toOptionList(list: List[String]) = if (list.isEmpty) None else Some(list)

      val (matchingErrors, nonMatchingErrors) = tail.partition(_.message == nextError.message)
      val nextErrorPaths                      = matchingErrors.flatMap(_.paths).flatten
      val newFlatError                        = nextError.copy(paths = toOptionList(nextError.paths.getOrElse(Nil).toList ++ nextErrorPaths))

      flattenErrors(List(nonMatchingErrors), flatErrors :+ newFlatError)
  }

}
