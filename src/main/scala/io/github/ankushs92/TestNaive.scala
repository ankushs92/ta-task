package io.github.ankushs92

import java.io.File

import io.github.ankushs92.model.{Airport, User, UserResult}

import scala.collection.mutable.ListBuffer
import scala.io.Source

object TestNaive extends App {
  private val USER_FILE = "/Users/ankushsharma/Desktop/test/user-geo-sample.csv.gz"
  private val AIRPORTS_FILE = "/Users/ankushsharma/travel_audience/src/main/resources/optd-airports-sample.csv.gz"

  val airportsIndex = ListBuffer[Airport]()
  val airports = Source.fromInputStream(Util.gis(new File(AIRPORTS_FILE)))
    .getLines().drop(1)
    .foreach { line =>
      val split = line.split(",")
      val iata = split(0)
      val lat = split(1).toDouble
      val lng = split(2).toDouble
      airportsIndex += Airport(iata, lat, lng)
    }

  val usersSrc = Source.fromInputStream(Util.gis(new File(USER_FILE)))
    .getLines().drop(1)
    .map { line =>
      val split = line.split(",")
      val uid = split(0)
      //These values are assumed to exist for each user input event. In production scenario, maybe a filter would be performed to consider only those users
      //that have both lat and lng values
      val lat = split(1).toDouble
      val lng = split(2).toDouble
      User(uid, lat, lng)
    }
    .map { user =>
      val closestAirport = airportsIndex
        .toStream
        .map{ airport =>
//          println(user)
          val haversine = Distance.haversine(user.lat, user.lng, airport.lat, airport.lng)
//          println(haversine)
          (airport, haversine)
        }
        .minBy{ _._2}

//      println(closestAirport)
      UserResult(user.uid, closestAirport._1.iata)
    }

  usersSrc.foreach { i=>
    println(i)
  }
}