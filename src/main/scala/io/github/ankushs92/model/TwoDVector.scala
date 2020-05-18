package io.github.ankushs92.model

object TwoDVector {
  val X_AXIS_IDX = 0
  val Y_AXIS_IDX = 1
}

trait TwoDVector {
  def getXDimValue(): Double

  def getYDimValue(): Double
}
