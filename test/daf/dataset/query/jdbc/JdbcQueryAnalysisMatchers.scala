/*
 * Copyright 2017 TEAM PER LA TRASFORMAZIONE DIGITALE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     ***REMOVED***://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ***REMOVED***.dataset.query.jdbc

import org.scalatest.matchers.{ HavePropertyMatchResult, HavePropertyMatcher }

object JdbcQueryAnalysisMatchers {

  def memoryReservation(value: Double) = new HavePropertyMatcher[JdbcQueryAnalysis, Double] {
    def apply(a: JdbcQueryAnalysis) = HavePropertyMatchResult(
      a.memoryReservation == value,
      "memoryReservation",
      value,
      a.memoryReservation
    )
  }

  def memoryEstimate(value: Double) = new HavePropertyMatcher[JdbcQueryAnalysis, Double] {
    def apply(a: JdbcQueryAnalysis) = HavePropertyMatchResult(
      a.memoryEstimation == value,
      "memoryEstimation",
      value,
      a.memoryEstimation
    )
  }

  def numSteps(value: Int) = new HavePropertyMatcher[JdbcQueryAnalysis, Int] {
    def apply(a: JdbcQueryAnalysis) = HavePropertyMatchResult(
      a.numSteps == value,
      "numSteps",
      value,
      a.numSteps
    )
  }

}
