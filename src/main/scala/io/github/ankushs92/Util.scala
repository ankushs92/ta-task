package io.github.ankushs92

import java.io.{BufferedInputStream, File, FileInputStream}
import java.lang.Math._
import java.util.zip.GZIPInputStream

import io.github.ankushs92.model.{Airport, User}

object Util {

  def gis(file: File) = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))
}
