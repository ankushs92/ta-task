package io.github.ankushs92.model

/**
 * The final result of our application
 * @param uid the uid of the user
 * @param iata the closest airport to the user
 */
case class UserResult(uid: String, iata: String) {
  /**
   * This string value will be written to the file sink
   */
  override def toString: String = new StringBuilder(uid)
    .append(",")
    .append(iata)
    .toString()

}
