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
import io.github.ankushs92.mapper.ClosestAirportMapper
import io.github.ankushs92.model.Constants.AIRPORT_FILE_KEY
import io.github.ankushs92.model.{User, UserResult}
import io.github.ankushs92.util.Util._
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object TravelAudienceStreamingJob {
  private val logger = Logger(this.getClass.getName)
  private val airportsFileName = "/optd-airports-sample.csv.gz"
  private val usersFileName = "/user-geo-sample.csv.gz"

  def main(args: Array[String]) {
    val start = System.currentTimeMillis()
    val outputPath = Option(args(0)).getOrElse(throw new RuntimeException("Please provide the path for output file!"))
    val usersFileAbsPath = getAbsPath(usersFileName)
    val airportsFileAbsPath = getAbsPath(airportsFileName)
    val streamEnv = StreamExecutionEnvironment.getExecutionEnvironment

    logger.info(s"Registering Airports file as Distributed Cache with cache name $AIRPORT_FILE_KEY")
    streamEnv.registerCachedFile(airportsFileAbsPath, AIRPORT_FILE_KEY)

    val usersStream: DataStream[User] =
      streamEnv
        .readTextFile(usersFileAbsPath)
        .filter {
          ignoreHeader
        }
        .map { line => User(line) }

    val usersClosestAirportStream: DataStream[UserResult] =
      usersStream
        .map {
          new ClosestAirportMapper
        }

    val fileSink = StreamingFileSink
      .forRowFormat(new Path(outputPath), new SimpleStringEncoder[UserResult]("UTF-8"))
      .build()

    usersClosestAirportStream.addSink(fileSink)

    //Execute the program
    streamEnv.execute("Travel Audience Task")
    val stop = System.currentTimeMillis()
    logger.info(s"Took ${stop - start} milliseconds to finish the job")
  }

  private def ignoreHeader = (string: String) => !string.contains("latitude")

}

