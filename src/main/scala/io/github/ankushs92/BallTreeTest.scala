package io.github.ankushs92

object BallTreeTest extends App{
//  private val USER_FILE = "/Users/ankushsharma/Desktop/test/user-geo-sample.csv.gz"
//  private val AIRPORTS_FILE = "/Users/ankushsharma/travel_audience/src/main/resources/optd-airports-sample.csv.gz"
//
//  val list = ListBuffer[Airport]()
//  val ballTree = new BallTree[Airport](new HaversineDistance, BallTree.ConstructionMethod.KD_STYLE, BallTree.PivotSelection.CENTROID)
//
//  val airports = Source.fromInputStream(Util.gis(new File(AIRPORTS_FILE)))
//    .getLines().drop(1)
//    .foreach { line =>
//      val split = line.split(",")
//      val iata = split(0)
//      val lat = split(1).toDouble
//      val lng = split(2).toDouble
////      list += Airport(iata, lat, lng)
//      ballTree.insert(Airport(iata, lat, lng))
//    }
//
////  val ballTree = new KDTree[Airport](list.toList.asJava, new HaversineDistance)
//
//  val usersSrc = Source.fromInputStream(Util.gis(new File(USER_FILE)))
//    .getLines().drop(1)
//    .map { line =>
//      val split = line.split(",")
//      val uid = split(0)
//      //These values are assumed to exist for each user input event. In production scenario, maybe a filter would be performed to consider only those users
//      //that have both lat and lng values
//      val lat = split(1).toDouble
//      val lng = split(2).toDouble
//      User(uid, lat, lng)
//    }
//    .map { user =>
//      val neighbourIndex = new IntList()
//      val distance = new DoubleList()
//
////      val closestAirport = ballTree.search(user, 1, neighbourIndex, distance)
////      println(neighbourIndex)
////      println(distance)
//      //      println(closestAirport)
//      UserResult(user.uid, ballTree.get(neighbourIndex.getI(0)).iata)
//    }
//
//  usersSrc.foreach { i=>
//    println(i)
//  }

}
