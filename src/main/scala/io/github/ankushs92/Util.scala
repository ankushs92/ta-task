package io.github.ankushs92

import java.io.{BufferedInputStream, File, FileInputStream}
import java.util.zip.GZIPInputStream

object Util {

  def gis(file: File) = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))

}
