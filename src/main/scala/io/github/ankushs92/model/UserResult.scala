package io.github.ankushs92.model

case class UserResult(uid: String, iata: String) {
  override def toString: String = new StringBuilder(uid)
    .append(",")
    .append(iata)
    .toString()
}
