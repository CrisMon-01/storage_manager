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

package ***REMOVED***.dataset

import java.io.FileNotFoundException

import ***REMOVED***.catalogmanager.{ MetaCatalog, StorageHdfs, StorageKudu }
import ***REMOVED***.filesystem.{ FileDataFormat, FileDataFormats, StringPathSyntax }

import scala.util.{ Failure, Success, Try }

sealed trait DatasetParams {

  def name: String

  def protocol: String

  def extraParams: ExtraParams

  def catalogUri: String

  final def param(key: String): Option[String] = extraParams.get(key)

}

case class KuduDatasetParams(table: String,
                             catalogUri: String,
                             extraParams: ExtraParams = Map.empty[String, String]) extends DatasetParams {

  final val name = table

  final val protocol = "kudu"

  def +(key: String, value: String): KuduDatasetParams = this.copy(
    extraParams = this.extraParams + { key -> value }
  )

}

case class FileDatasetParams(path: String,
                             catalogUri: String,
                             format: FileDataFormat,
                             extraParams: ExtraParams = Map.empty[String, String]) extends DatasetParams {

  final val name = path.asHadoop.getName

  final val protocol = "hdfs"

  def +(key: String, value: String): FileDatasetParams = this.copy(
    extraParams = this.extraParams + { key -> value }
  )

}

object DatasetParams {

  private val separatorRegex = """'separatorChar'.=.'.*'.,'""".r

  private def readPhysicalPath(catalog: MetaCatalog) = catalog.operational.physical_uri match {
    case Some(uri) => Success(uri)
    case None      => Failure { new FileNotFoundException(s"Cannot find a physical location for path [${catalog.operational.logical_uri}]") }
  }

  private def readDataFormat(catalog: MetaCatalog, hdfsInfo: StorageHdfs) = hdfsInfo.param.getOrElse { "format=parquet" }.split("=") match {
    case Array("format", FileDataFormats(format)) => Success(format)
    case Array(unknownKey, unknownValue)          => Failure {
      new RuntimeException(s"Unknown key/value pair [$unknownKey = $unknownValue] encountered in catalog while expecting [format] for catalog path [${catalog.operational.logical_uri}]")
    }
    case _                                        => Failure {
      new RuntimeException(s"Unknown param value encountered for catalog path [${catalog.operational.logical_uri}]")
    }
  }

  private def readSeparator(catalog: MetaCatalog) = Try {
    separatorRegex.findFirstIn { catalog.dataschema.kyloSchema getOrElse "{}" }.getOrElse(",").split(" ,").headOption.flatMap {
      _.replace("""\\\\""", "").replaceAll("'", "").split(" = ").lastOption
    }.map { _.trim }
  }

  private def readTheme(catalog: MetaCatalog) = Try {
    catalog.operational.theme match {
      case "" | "unknown" | null => None
      case theme                 => Some(theme)
    }
  }

  private def readSubTheme(catalog: MetaCatalog) = Try {
    catalog.operational.subtheme match {
      case "" | "unknown" | null => None
      case theme                 => Some(theme)
    }
  }

  private def addParam(key: String, value: Option[String], extraParams: ExtraParams = Map.empty[String, String]) =
    value.map { v => extraParams + (key -> v) } getOrElse extraParams

  private def createParams(params: (String, Option[String])*) = Success {
    params.foldLeft(Map.empty[String, String]) { (params, pair) => addParam(pair._1, pair._2, params) }
  }

  private def readTable(catalog: MetaCatalog, kuduInfo: StorageKudu) = kuduInfo.table_name match {
    case Some(table) => Success(table)
    case None        => Failure { new RuntimeException(s"Unknown Kudu table name for catalog path [${catalog.operational.logical_uri}]") }
  }

  private def fromHdfs(catalog: MetaCatalog, hdfsInfo: StorageHdfs) = for {
    path        <- readPhysicalPath(catalog)
    format      <- readDataFormat(catalog, hdfsInfo)
    separator   <- readSeparator(catalog)
    theme       <- readTheme(catalog)
    subTheme    <- readSubTheme(catalog)
    extraParams <- createParams("separator" -> separator, "theme" -> theme, "subTheme" -> subTheme)
  } yield FileDatasetParams(
    path        = path,
    catalogUri  = catalog.operational.logical_uri,
    format      = format,
    extraParams = extraParams
  )

  private def fromKudu(catalog: MetaCatalog, kuduInfo: StorageKudu) = for {
    table       <- readTable(catalog, kuduInfo)
    extraParams <- createParams("separator" -> Some(","))
  } yield KuduDatasetParams(table, catalog.operational.logical_uri, extraParams)

  def fromCatalog(catalog: MetaCatalog): Try[DatasetParams] = catalog.operational.storage_info.flatMap { info => info.hdfs orElse info.kudu } match {
    case Some(hdfsInfo: StorageHdfs) => fromHdfs(catalog, hdfsInfo)
    case Some(kuduInfo: StorageKudu) => fromKudu(catalog, kuduInfo)
    case Some(_) | None              => Failure { new IllegalArgumentException(s"Unable to extract valid parameters for logical path [${catalog.operational.logical_uri}]") }
  }

}