package io.github.ankushs92.model

case class User(uid: String, lat: Double, lng: Double) extends TwoDVector {
  override def getXDimValue(): Double = lat

  override def getYDimValue(): Double = lng
}

object User {
  def apply(line: String): User = {
    val split = line.split(",")
    val uid = split(0)
    //We assume that lat and lng values exists in a user
    val lat = split(1).toDouble
    val lng = split(2).toDouble
    User(uid, lat, lng)
  }
}