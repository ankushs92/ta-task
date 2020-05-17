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

import akka.event.slf4j.Logger
import io.github.ankushs92.Util.gis
import io.github.ankushs92.model.Constants.AIRPORT_FILE_KEY
import io.github.ankushs92.model.{Airport, User, UserResult}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.io.Source

object StreamingJob {
  private val logger = Logger(this.getClass.getName)

  def main(args: Array[String]) {
    val usersFile = args(0)
    val airportsFile = args(1)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    logger.info(s"Registering Airports file as Distributed Cache with cache name $AIRPORT_FILE_KEY")
    env.registerCachedFile(airportsFile, AIRPORT_FILE_KEY)

    val usersStream: DataStream[User] = env.readTextFile(usersFile)
      .filter { ignoreHeader }
      .map { line => User(line) }

    val usersClosestAirportStream = usersStream.map { new ClosestAirportEnrichMapper }

    //    usersClosestAirportStream.print()

    //Execute the program
    val start = System.currentTimeMillis()
    env.execute("Travel Audience Task")
    println(System.currentTimeMillis() - start)
  }

  private def ignoreHeader = (string : String) => !string.contains("latitude")

}

class ClosestAirportEnrichMapper extends RichMapFunction[User, UserResult] {
  private val logger = Logger(this.getClass.getName)
  private val airportsIndex = new SpatialIndex[Airport]

  override def open(config: Configuration): Unit = {
    val airportsFile = getRuntimeContext.getDistributedCache.getFile(AIRPORT_FILE_KEY)
    val src = Source.fromInputStream(gis(airportsFile))
    val start = System.currentTimeMillis()
    src.getLines()
      .drop(1) // csv header
      .foreach { line => airportsIndex += Airport(line) }
    val stop = System.currentTimeMillis()
    val timeTaken = stop - start
    logger.info(s"Time taken to construct Ball Tree : $timeTaken ms")
    src.close()
  }

  override def map(user: User): UserResult = {
    val nearestAirport = airportsIndex.findNearestNeighbour(user)
    UserResult(user.uid, nearestAirport.iata)
  }

}
