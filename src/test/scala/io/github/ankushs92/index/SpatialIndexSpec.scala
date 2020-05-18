package io.github.ankushs92.index

import java.io.{BufferedInputStream, InputStream}
import java.util.zip.GZIPInputStream

import io.github.ankushs92.model.{Airport, User}
import org.scalatest._

import scala.io.Source

class SpatialIndexSpec extends FlatSpec {

  private val airportsFile = "/optd-airports-sample.csv.gz"
  private val usersFile = "/user-geo-sample.csv.gz"

  "SpatialIndex implemented as a BallTree " should " yield same result as naive implementation" in {
    val airportsSrc = Source.fromInputStream(gzipIs(getClass.getResourceAsStream(airportsFile)))
    val usersSrc = Source.fromInputStream(gzipIs(getClass.getResourceAsStream(usersFile)))

    val airports = airportsSrc
      .getLines()
      .drop(1)
      .map { line => Airport(line) }
      .toList

    //Take the first 1000 users
    //Build a cache of [user.uid, closestairport.iata] pairs. This is built on top of naive implementation
    val users = usersSrc
      .getLines()
      .slice(1, 1001) // Ignoring header
      .map { line => User(line) }
      .toList

    val userResultsNaive = users
      .map { user =>
        val closestAirport = airports
          .map { airport => (airport, Distance.haversine(user.lat, user.lng, airport.lat, airport.lng)) }
          .minBy {
            _._2
          }
          ._1
        (user.uid, closestAirport.iata)
      }
      .toMap

    //Now comes the spatial index implementation
    val spatialIndex = new SpatialIndex[Airport]
    airports.foreach { airport => spatialIndex += airport }

    //This is the actual check for the first 1000 (users,closestAirport) pairs done via naive implementation against spatial index
    users.foreach { user =>
      val closestAirportViaNaive = userResultsNaive(user.uid)
      val closestAiportViaSpatialIndex = spatialIndex.findNearestNeighbour(user).iata
      assert(closestAirportViaNaive == closestAiportViaSpatialIndex)
    }

    airportsSrc.close()
    usersSrc.close()
  }

  def gzipIs(is: InputStream) = new GZIPInputStream(new BufferedInputStream(is))
}