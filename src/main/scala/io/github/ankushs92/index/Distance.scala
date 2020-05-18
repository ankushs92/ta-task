package io.github.ankushs92.index

import io.github.ankushs92.model.TwoDVector
import jsat.linear.Vec
import jsat.linear.distancemetrics.DistanceMetric


class HaversineDistance extends DistanceMetric {

  override def dist(vec1: Vec, vec2: Vec): Double = {
    val lat1 = vec1.get(TwoDVector.X_AXIS_IDX)
    val lng1 = vec1.get(TwoDVector.Y_AXIS_IDX)
    val lat2 = vec2.get(TwoDVector.X_AXIS_IDX)
    val lng2 = vec2.get(TwoDVector.Y_AXIS_IDX)
    Distance.haversine(lat1, lng1, lat2, lng2)
  }

  //is d(x,y) = d(y,x)
  override def isSymmetric: Boolean = true

  //is d(x,y) <= d(x,z) + d(z,y), for any x, y , z in 2-d space
  override def isSubadditive: Boolean = true

  //is d(x,y) = 0 => x = y
  override def isIndiscemible: Boolean = true

  //The max possible distance between two vectors
  override def metricBound(): Double = Double.PositiveInfinity
}

object Distance {
  //Copied without shame from stackoverflow
  private val R = 6372.8 //radius in km
  def haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double) = {
    val dLat = (lat1 - lat2).toRadians
    val dLon = (lng1 - lng2).toRadians
    val a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1.toRadians) * Math.cos(lat2.toRadians)
    val c = 2 * Math.asin(Math.sqrt(a))
    R * c
  }

}