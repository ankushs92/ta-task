/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ankushs92

import java.io.{BufferedInputStream, File, FileInputStream}
import java.util.zip.GZIPInputStream

import io.github.ankushs92.model.{GeoNames, User, UserResult}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.{ConfigOption, Configuration}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
 * To package your application into a JAR file for execution,
 * change the main class in the POM.xml file to this class (simply search for 'mainClass')
 * and run 'mvn clean package' on the command line.
 */
object BatchJob {
  private val USER_FILE = " /Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz"
  private val GEONAMES_FILE = " /Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz"

  def main(args: Array[String]) {
    // set up the batch execution environment
    val config = new Configuration()
    config.set
    val env = ExecutionEnvironment.getExecutionEnvironment
    val totalMemory = Runtime.getRuntime.maxMemory()
    val cores = Runtime.getRuntime.availableProcessors()
    env.registerCachedFile(GEONAMES_FILE, "geoNames")

    val usersDs : DataSet[User] = env.readCsvFile(USER_FILE, ignoreFirstLine = true)

    usersDs.map(new MyMapper)
        .print()

    // execute program
    env.execute("Travel Audience Task")
  }
}

// extend a RichFunction to have access to the RuntimeContext
class MyMapper extends RichMapFunction[User, UserResult] {

  private val geoNames = ListBuffer[GeoNames]()
  override def open(config: Configuration): Unit = {
    val geoNamesFile = getRuntimeContext.getDistributedCache.getFile("geoNames")
    val start = System.currentTimeMillis()
    println("Ok I think this is taking some time ")
    val src = Source.fromInputStream(gis(geoNamesFile)) //We stream the file instead of loading everything in memory
    src.getLines()
      .drop(1) // Header
      .foreach { line =>
        val split = line.split(",")
        val iata = split(0)
        val lat = split(1).toDouble
        val lng = split(2).toDouble
        geoNames += GeoNames(iata, lat, lng)
    }
    val stop = System.currentTimeMillis()
    println("Time taken " + (stop - start))
  }

  override def map(user: User): UserResult = {
    val min = geoNames
      .toStream
      .map {geoName => (geoName, euclideanDist(user, geoName)) }
      .minBy { _._2 }
      ._1
    UserResult(user.uid, min.iata)
  }

  private def euclideanDist(user : User, geoNames : GeoNames) = {
    val userLat = user.lat
    val userLng = user.lng
    val geoLat = geoNames.lat
    val geoLng = geoNames.lng
    Math.sqrt(
      Math.pow(userLat - geoLat, 2) + Math.pow(userLng - geoLng, 2)
    )
  }
  def gis(file: File) = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))
}
