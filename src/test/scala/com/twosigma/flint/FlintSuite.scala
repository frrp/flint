/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.flint

import java.io.{ File, InputStream }
import java.nio.file.{ Files, Path, StandardCopyOption }

import org.apache.commons.io.FilenameUtils
import org.scalatest.{ FlatSpec, PropSpec }
import play.api.libs.json.{ JsValue, Json }

import scala.io.Source

object FlintSuite {

  /**
   * Access resources from either resource files or packaged resources from .jar
   *
   * @note This potentially copies the resource outside of a .jar, duplicating uncompressed data
   * Although the copies are cleaned up afterwards, it could potentially use a lot of resources.
   * @param resource The relative path of resource, e.g. foo/bar/data.csv.
   * @param prefix   The prefix of temporary filename that the resource will be copied to. Default "".
   * @param suffix   The suffix of temporary filename that the resource will be copied to. Default "".
   * @param codeBlock A function that takes a resource file path as input.
   * @return the return value of `codeBlock`.
   */
  def withResource[T](resource: String, prefix: String = "", suffix: String = "")(codeBlock: String => T): T = {
    var source: String = getClass.getResource(resource).getPath
    var sourcePath: Path = null
    val sourceExists = new File(source).exists
    if (!sourceExists) {
      val in: InputStream = getClass.getResourceAsStream(resource)
      sourcePath = Files.createTempFile(prefix, suffix)
      source = sourcePath.toString
      Files.copy(in, sourcePath, StandardCopyOption.REPLACE_EXISTING)
    }

    var returnValue: T = null.asInstanceOf[T]
    try {
      returnValue = codeBlock(source)
    } finally {
      if (!sourceExists) {
        Files.delete(sourcePath)
      }
    }
    returnValue
  }
}

trait FlintSuite extends FlatSpec with SharedSparkContext {

  /**
   * Access resources from either resource files or packaged resources from .jar
   *
   * @note This potentially copies the resource outside of a .jar, duplicating uncompressed data
   * Although the copies are cleaned up afterwards, it could potentially use a lot of resources.
   * @param resource The relative path of resource, e.g. foo/bar/data.csv.
   * @param codeBlock A function that takes a resource file path as input.
   * @return the return value of `codeBlock`.
   */
  def withResource[T](resource: String)(codeBlock: String => T): T = {
    val suffix = "." + FilenameUtils.getExtension(resource)
    FlintSuite.withResource(resource, "", suffix)(codeBlock)
  }
}
