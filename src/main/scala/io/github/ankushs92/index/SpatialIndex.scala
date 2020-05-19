package io.github.ankushs92.index

import io.github.ankushs92.model.{TwoDVector, User}
import jsat.linear.Vec
import jsat.linear.vectorcollection.BallTree
import jsat.utils.{DoubleList, IntList}


class SpatialIndex[T <: TwoDVector] {

  private val ballTree = new BallTree[BallTreeNode[T]](new HaversineDistance, BallTree.ConstructionMethod.TOP_DOWN_FARTHEST, BallTree.PivotSelection.MEDOID)
  private val ONE_NEIGHBOUR = 1

  /**
   * Add a value to the spatial index
   */
  def +=(value: T): Unit = ballTree.insert(BallTreeNode(value))

  def findNearestNeighbour(query: User): T = {
    val neighboursIndex = new IntList()
    val distances = new DoubleList()
    val queryNode = BallTreeNode(query)
    ballTree.search(queryNode, ONE_NEIGHBOUR, neighboursIndex, distances)
    val nearestNeighbour = ballTree.get(neighboursIndex.getI(0))
    nearestNeighbour.value
  }

}

sealed case class BallTreeNode[T <: TwoDVector](value: T) extends Vec {
  override def length(): Int = 2

  override def get(idx: Int): Double = idx match {
    case TwoDVector.X_AXIS_IDX => value.getXDimValue()
    case TwoDVector.Y_AXIS_IDX => value.getYDimValue()
  }

  override def set(idx: Int, v: Double): Unit = ???

  override def isSparse: Boolean = false

  override def setLength(i: Int): Unit = ???

  override def clone(): Vec = this
}