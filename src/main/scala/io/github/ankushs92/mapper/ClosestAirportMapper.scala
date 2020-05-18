package io.github.ankushs92.mapper

import akka.event.slf4j.Logger
import io.github.ankushs92.index.SpatialIndex
import io.github.ankushs92.model.Constants.AIRPORT_FILE_KEY
import io.github.ankushs92.model.{Airport, User, UserResult}
import io.github.ankushs92.util.Util.readGzipFile
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

import scala.io.Source

/**
 * A mapper that takes a User object, finds the nearest airport, and packages the (user.uid, closestairport.iata)
 * pair in a UserResult object
 */
class ClosestAirportMapper extends RichMapFunction[User, UserResult] {
  //Lazy put here to avoid flink serialization exception
  private lazy val airportsIndex = new SpatialIndex[Airport]
  private val logger = Logger(this.getClass.getName)

  override def open(config: Configuration): Unit = {
    val airportsFile = getRuntimeContext.getDistributedCache.getFile(AIRPORT_FILE_KEY)
    val src = Source.fromInputStream(readGzipFile(airportsFile))
    val start = System.currentTimeMillis()
    src.getLines()
      .drop(1) // csv header
      .foreach { line => airportsIndex += Airport(line) }
    val stop = System.currentTimeMillis()
    val timeTaken = stop - start
    logger.info(s"Time taken to construct SpatialIndex : $timeTaken ms")
    src.close()
  }

  override def map(user: User): UserResult = {
    val nearestAirport = airportsIndex.findNearestNeighbour(user)
    UserResult(user.uid, nearestAirport.iata)
  }
}
