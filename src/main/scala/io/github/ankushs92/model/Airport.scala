package io.github.ankushs92.model


case class Airport(iata: String, lat: Double, lng: Double) extends TwoDVector {
  override def getXDimValue(): Double = lat

  override def getYDimValue(): Double = lng
}

object Airport {

  def apply(line: String): Airport = {
    val split = line.split(",")
    val iata = split(0)
    val lat = split(1).toDouble
    val lng = split(2).toDouble
    Airport(iata, lat, lng)
  }

}