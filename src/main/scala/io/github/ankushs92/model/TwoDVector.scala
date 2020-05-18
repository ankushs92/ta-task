package io.github.ankushs92.model

object TwoDVector {
  val X_AXIS_IDX = 0
  val Y_AXIS_IDX = 1
}

/**
 * Each entry added to the 2-d spatial index will be a subtype of this trait.
 */
trait TwoDVector {

  def getXDimValue(): Double

  def getYDimValue(): Double
}
