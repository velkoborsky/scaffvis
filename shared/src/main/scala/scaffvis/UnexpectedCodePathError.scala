package scaffvis

final class UnexpectedCodePathError(msg: String) extends Error(msg) {
  def this() = this("this should never happen")
}
