package io.github.ankushs92

import java.io.{BufferedInputStream, File, FileInputStream}
import java.lang.Math._
import java.util.zip.GZIPInputStream

import io.github.ankushs92.model.{Airport, User}

object Util {
  //Copied without shame from stackoverflow
  private val R = 6372.8  //radius in km
  def haversine(user : User, airport : Airport) = {
    val dLat=(airport.lat - user.lat).toRadians
    val dLon=(airport.lng - airport.lng).toRadians

    val a = pow(sin(dLat / 2), 2) + pow(sin(dLon/2),2) * cos(airport.lat.toRadians) * cos(airport.lng.toRadians)
    val c = 2 * asin(sqrt(a))
    R * c
  }

  def gis(file: File) = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))
}
