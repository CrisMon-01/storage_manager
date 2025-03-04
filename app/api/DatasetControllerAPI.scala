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

package api

import ***REMOVED***.dataset.query.Query
import io.swagger.annotations._
import javax.ws.rs.QueryParam
import play.api.mvc.{ Action, AnyContent }

@Api(value = "dataset-manager")
trait DatasetControllerAPI {

  @ApiOperation(
    value = "Get a dataset based on the dataset id",
    produces = "application/json",
    ***REMOVED***Method = "GET",
    authorizations = Array(new Authorization(value = "basicAuth")),
    protocols = "***REMOVED***, ***REMOVED***"
  )
  def getSchema(@ApiParam(value = "the uri to access the dataset", required = true) uri: String): Action[AnyContent]


  @ApiOperation(
    value = "Get a dataset based on the dataset id",
    produces = "application/json",
    ***REMOVED***Method = "GET",
    authorizations = Array(new Authorization(value = "basicAuth")),
    protocols = "***REMOVED***, ***REMOVED***"
  )
  def getDataset(@ApiParam(value = "the uri to access the dataset", required = true)
                 uri: String,
                 @ApiParam(value = "the format the downloaded data should be converted", required = false, defaultValue = "json")
                 @QueryParam("format")
                 format: String,
                 @ApiParam(value = "the method used to perform the data conversions", required = false, defaultValue = "quick")
                 @QueryParam("method")
                 method: String,
                 @ApiParam(value = "the maximum number of rows returned by this request", required = false)
                 @QueryParam("limit")
                 limit: Option[Int]): Action[AnyContent]

  @ApiOperation(
    value = "Get a dataset based on the dataset id",
    produces = "application/json",
    consumes = "application/json",
    ***REMOVED***Method = "POST",
    authorizations = Array(new Authorization(value = "basicAuth")),
    protocols = "***REMOVED***, ***REMOVED***"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "query",
      value = "A valid query",
      required = true,
      dataType = "***REMOVED***.dataset.Query",
      paramType = "body"
    )
  ))
  def queryDataset(@ApiParam(value = "the uri to access the dataset", required = true)
                   uri: String,
                   @ApiParam(value = "the format the downloaded data should be converted", required = false, defaultValue = "json")
                   @QueryParam("format")
                   format: String,
                   @ApiParam(value = "the method used to perform the data conversions", required = false, defaultValue = "quick")
                   @QueryParam("method")
                   method: String): Action[Query]

}
