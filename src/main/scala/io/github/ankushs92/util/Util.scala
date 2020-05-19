package io.github.ankushs92.util

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

object Util {

  def readGzipFile(file: File) = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)))

  def flinkReadableAbsPath(absPath : String) = "file://" + absPath
}
