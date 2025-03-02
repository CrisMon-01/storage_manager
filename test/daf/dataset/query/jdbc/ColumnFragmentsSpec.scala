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

import ***REMOVED***.dataset.query._
import org.scalatest.{ MustMatchers, WordSpec }
import doobie.implicits.toSqlInterpolator

import scala.annotation.tailrec
import scala.util.Success

class SelectFragmentSpec extends WordSpec with MustMatchers {

  "A [select] fragment writer" must {

    "serialize a [select] clause in SQL" in {
      ColumnFragments.select { SelectClauses.simple }.run.map { _._1.toString } must be {
        Success { fr"SELECT col1, col2 AS alias1, 1, 'string' AS alias2, MAX(col3) AS alias3, SUM(true)".toString }
      }
    }

    "create a column/alias reference set" in {
      ColumnFragments.select { SelectClauses.simple }.run.get._2 must have (
        ColumnReferenceMatchers hasColumn "col1",
        ColumnReferenceMatchers hasColumn "col2",
        ColumnReferenceMatchers hasColumn "1",
        ColumnReferenceMatchers hasColumn "'string'",
        ColumnReferenceMatchers hasColumn "col3",
        ColumnReferenceMatchers hasColumn "true",
        ColumnReferenceMatchers hasAlias  "alias1",
        ColumnReferenceMatchers hasAlias  "alias2",
        ColumnReferenceMatchers hasAlias  "alias3"
      )
    }

    "serialize a very long [select] without stack overflow" in {
      ColumnFragments.select { SelectClauses.nested }.run must be { 'Success }
    }

    "fail serialization when sql is injected in a column name" in {
      ColumnFragments.select { SelectClauses.injectNamed }.run must be { 'Failure }
    }

    "escape quotes in value strings" in {
      ColumnFragments.select { SelectClauses.injectValue }.run.map { _._1.toString } must be {
        Success { fr"""SELECT '\' SELECT col2 FROM table WHERE \'\' == \''""".toString }
      }
    }

  }

}

object SelectClauses {

  val simple = SelectClause {
    Seq(
      NamedColumn("col1"),
      NamedColumn("col2") as "alias1",
      ValueColumn(1),
      ValueColumn("string") as "alias2",
      Max(NamedColumn("col3")) as "alias3",
      Sum(ValueColumn(true))
    )
  }

  val injectNamed = SelectClause {
    Seq(
      NamedColumn("SELECT col2 FROM table")
    )
  }

  val injectValue = SelectClause {
    Seq(
      ValueColumn("' SELECT col2 FROM table WHERE '' == '")
    )
  }

  @tailrec
  private def nest(column: Column, n: Int = 10000): Column = if (n == 0) column else nest(Sum(column), n - 1)

  val nested = SelectClause {
    Seq { nest(ValueColumn(true)) }
  }

}