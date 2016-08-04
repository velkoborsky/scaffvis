package scaffvis.layout

trait Gradient extends ((Double) => Color) {

  /**
    *
    * @param x 0 <= x <= 1
    */
  override def apply(x: Double): Color

  def name: String

}

case class Color(r: Int, g: Int, b: Int) {
  def css = s"rgb($r, $g, $b)"
}

object Gradient {

  case object BlueYellow extends LinearRBGInterpolationTwoColorGradient {
    override val name = "Blue-Yellow"
    override val low = Color(66, 178, 233)
    override val high = Color(245, 194, 24)
  }

  case object BlueRed extends LinearRBGInterpolationTwoColorGradient {
    override val name = "Blue-Red"
    override val low = Color(52, 52, 171)
    override val high = Color(171, 52, 52)
  }

  case object WhiteRed extends LinearRBGInterpolationTwoColorGradient {
    override val name = "White-Red"
    override val low = Color(210, 210, 210)
    override val high = Color(240, 30, 30)
  }

  lazy val values = Seq[Gradient](BlueYellow, BlueRed, WhiteRed)

}

trait LinearRBGInterpolationTwoColorGradient extends Gradient {

  def low: Color
  def high: Color

  override def apply(x: Double): Color = {
    if(0 <= x && x <= 1) {
      val z = 1-x
      Color((z*low.r + x*high.r).toInt, (z*low.g + x*high.g).toInt, (z*low.b + x*high.b).toInt)
    } else {
      println(s"invalid gradient input: $x")
      Color(255, 50, 50)
    }
  }
}