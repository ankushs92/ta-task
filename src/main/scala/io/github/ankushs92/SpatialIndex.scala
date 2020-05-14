package io.github.ankushs92

import java.lang.Math.{asin, cos, pow, sin, sqrt}

import io.github.ankushs92.model.{Airport, TwoDVector, User}

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag


/**
 * Almost Generic implementation of KDTree. Not thread safe.
 *
 * @tparam T
 */
case class SpatialIndex[T <: TwoDVector](implicit c: ClassTag[T]) {

  private val coordinates = ListBuffer[T]()
  private val kdTree = KDTree[T](coordinates)

  def +=(value: T) : Unit = coordinates += value
  def build(): Unit = kdTree.build()
  def findNearestNeighbour(search: User): T = kdTree.nearestNeighbour(search.lat, search.lng)

}

case class KDTree[T <: TwoDVector](values: ListBuffer[T])(implicit c: ClassTag[T]) {

  private var rootNode : Option[KDNode[T]] = None

  def insert(value: T, depth: Int, nodeOpt: Option[KDNode[T]]): Option[KDNode[T]] = {
    val axis = depth % 2
    nodeOpt match {
      case Some(_) => Some(KDNode[T](value, None, None))
      case None =>
        val node = nodeOpt.get
        val nodeValue = node.value
        if (value.forAxis(axis) < nodeValue.forAxis(axis)) {
          node.left = insert(value, axis + 1, node.left)
        }
        else {
          node.right = insert(value, axis + 1, node.right)
        }
        nodeOpt
    }
  }

  def build() : Unit = {
    val sorted = values.distinct.sortWith(_.getX() < _.getX())
    val medianIndex = sorted.length / 2
    val median = sorted(medianIndex)
    rootNode = Option(KDNode[T](median, None, None))
    sorted.remove(medianIndex)
    values.foreach { point => rootNode = insert(point, 0, rootNode) }
  }


  def nearestNeighbour(xCord : Double, yCord : Double) : T = {
    var minDist : Double = 0.0
    var closest : Option[T] = None
    rootNode match {
      case Some(_) => searchRec(xCord, yCord, rootNode, rootNode, Double.MaxValue)
      case None =>
    }
    if(rootNode.isDefined) {

    } else
  }

  private def searchRec(
                         xCord : Double,
                         yCord : Double,
                         node : KDNode[T],
                         ref : KDNode[T],
                         min : Double
                       ) : T = {
    if(node.isLeaf) {
      val leaf = node.value
      val dist = haversine(xCord, yCord, leaf.getX(), leaf.getY())

    }
    null
  }

  override def toString: String = rootNode.getOrElse(Nil).toString

  //Copied without shame from stackoverflow
  private val R = 6372.8 //radius in km

  def haversine(lat1 : Double, lng1 : Double, lat2 : Double, lng2: Double) = {
    val dLat = (lat1 - lat2).toRadians
    val dLon = (lng1 - lng2).toRadians

    val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat1.toRadians) * cos(lat2.toRadians)
    val c = 2 * asin(sqrt(a))
    R * c
  }

}


case class KDNode[T](value: T, var left: Option[KDNode[T]], var right: Option[KDNode[T]]) {

  def isLeaf : Boolean = left.isEmpty && right.isEmpty
  override def toString: String = s"Value : $value, LeftTree : $left, RightTree : $right"
}


object T extends App {
  val list = Array((7, 2), (5, 4), (9, 6), (4, 7), (8, 1), (2, 3))
  //  val kd = new KDTree[Airport]
  //  list.foreach { x =>
  //    kd += Airport("", x._1, x._2)
  //  }
  //  kd.buildTree()

}