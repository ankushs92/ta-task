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

import io.github.ankushs92.Util.gis
import io.github.ankushs92.model.{Airport, User, UserResult}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.io.Source

/**
 * To package your application into a JAR file for execution,
 * change the main class in the POM.xml file to this class (simply search for 'mainClass')
 * and run 'mvn clean package' on the command line.
 */
object BatchJob {
  //  private val logger = Logger(this.getClass)
  private val USER_FILE = "/Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz"
  private val AIRPORTS_FILE = " /Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz"

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.registerCachedFile(AIRPORTS_FILE, "airports")

    val usersStream: DataStream[User] = env.readTextFile(USER_FILE)
      .filter { line => !line.contains("latitude") }
      .map { line =>
        val split = line.split(",")
        val uid = split(0)
        //These values are assumed to exist for each user input event. In production scenario, maybe a filter would be performed to consider only those users
        //that have both lat and lng values
        val lat = split(1).toDouble
        val lng = split(2).toDouble
        User(uid, lat, lng)
      }

    usersStream.map {
      new MyMapper
    }
      .print()

    //Execute the program
    env.execute("Travel Audience Task")
  }
}

class MyMapper extends RichMapFunction[User, UserResult] {

  private val airportsIndex = new SpatialIndex[Airport]

  override def open(config: Configuration): Unit = {
    val airportsFile = getRuntimeContext.getDistributedCache.getFile("airports")
    val src = Source.fromInputStream(gis(airportsFile))
    src.getLines()
      .drop(1) // csv header
      .foreach { line =>
        val split = line.split(",")
        val iata = split(0)
        val lat = split(1).toDouble
        val lng = split(2).toDouble
        airportsIndex += Airport(iata, lat, lng)
      }
    airportsIndex.buildTree()
    src.close()
  }

  override def map(user: User): UserResult = {
    val nearestAirport = airports.findNearestNeighbour(user)
    UserResult(user.uid, nearestAirport.iata)
  }

}
