package io.github.ankushs92.model

object TwoDVector {
  val X_AXIS = 0
  val Y_AXIS = 1
}

trait TwoDVector {

  def getX(): Double

  def getY(): Double

  def forAxis(holder: Int): Double = holder match {
    case TwoDVector.X_AXIS => getX()
    case TwoDVector.Y_AXIS => getY()
  }

}

case class Airport(iata: String, lat: Double, lng: Double) extends TwoDVector {
  override def getX(): Double = lat

  override def getY(): Double = lng
}
