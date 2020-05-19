package io.github.ankushs92

import akka.event.slf4j.Logger
import io.github.ankushs92.mapper.ClosestAirportMapper
import io.github.ankushs92.model.Constants._
import io.github.ankushs92.model.{User, UserResult}
import io.github.ankushs92.util.Util._
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

/**
 * Entry point to a streaming flink job
 */
object TravelAudienceStreamingJob {
  private val logger = Logger(this.getClass.getName)

  def main(args: Array[String]) {
    val start = System.currentTimeMillis()
    val usersFilePath = Option(args(0)).getOrElse(throw new RuntimeException("Please provide the path for users file!"))
    val airportsFilePath = Option(args(1)).getOrElse(throw new RuntimeException("Please provide the path for airport file!"))
    val outputPath = Option(args(2)).getOrElse(throw new RuntimeException("Please provide the path for output file!"))
    val usersFileAbsPath = flinkReadableAbsPath(usersFilePath)
    val airportsFileAbsPath = flinkReadableAbsPath(airportsFilePath)

    val streamEnv = StreamExecutionEnvironment.getExecutionEnvironment
    logger.info(s"Registering Airports file as Distributed Cache with key $AIRPORT_FILE_KEY")
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
    val timeTaken = toSeconds(stop - start)
    logger.info(s"Took $timeTaken seconds to finish the job")
  }

  private def ignoreHeader = (string: String) => !string.contains("latitude")
  private def toSeconds = (ms : Long) => ms / 1000

}

