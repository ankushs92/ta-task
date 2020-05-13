package io.github.ankushs92

import java.util

import io.github.ankushs92.model.TwoDVector._
import io.github.ankushs92.model.{Airport, TwoDVector, User}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Sorting
import scala.reflect.ClassTag

/**
 * Almost Generic implementation of KDTree. Not thread safe.
 * @tparam T
 */
case class KDTree[T <: TwoDVector](implicit c: ClassTag[T]) {

  private val points = ListBuffer[T]()
  private var rootNode = Option[KDNode[T]](null)

  def +=(value : T) =  points += value

  def buildTree() = {
    val sorted = points.sortWith(_.getX() < _.getX())
    val median = sorted(sorted.length / 2)
    rootNode = Option(KDNode[T](median, None, None))
    sorted.remove(sorted.length / 2)
    points.foreach { point => rootNode = insert(point, 0, rootNode) }
  }

  def findNearestNeighbour(searchQuery : User) : T = ???

  private def insert(value : T, depth : Int, nodeOpt : Option[KDNode[T]]) : Option[KDNode[T]] = {
    val axis = depth % 2
//    println(value)
//    println(nodeOpt)
    if(nodeOpt.isEmpty) {
        Option(KDNode[T](value, None, None))
    }
    else {
      val node = nodeOpt.get
      val nodeValue = node.value
      if(value == nodeValue) {
        //Error!
        println("error")
      }
      else if(value.from(axis) < nodeValue.from(axis)) {
        node.left = insert(value, axis + 1, node.left)
      }
      else {
        node.right = insert(value, axis + 1, node.right)
      }
      nodeOpt
    }
  }


//  val ordering : Ordering[T] =  Ordering.by[T, Double](_.getX())
//
//  Sorting.quickSort(points)(ordering)

    override def toString: String = {
    val sb = new mutable.StringBuilder()
    val root = rootNode.get
    while(root.hasNext) {
      val left = root.left.orNull
      val right = root.right.orNull
//      root =
      sb.append(s"Left ${left.value}, Right ${right.value}")
    }
    sb.toString()
  }
}

case class KDNode[T](value : T, var left : Option[KDNode[T]], var right : Option[KDNode[T]]) {
    def hasNext = left.isDefined || right.isDefined
}

object T extends App {
  val list = Array((7,2), (5,4), (9,6), (4,7), (8,1), (2,3))
  val kd = new KDTree[Airport]
  list.foreach { x =>
    kd += Airport("", x._1, x._2)
  }
  kd.buildTree()

}