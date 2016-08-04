package scaffvis.layout

case class Rect(x: Double, y: Double, w: Double, h: Double) {
  def area = w * h
  lazy val minSize = Math.min(w, h)
}
