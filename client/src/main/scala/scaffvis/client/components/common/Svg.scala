package scaffvis.client.components.common

import scaffvis.layout.Rect
import japgolly.scalajs.react.vdom.ReactTagOf
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.html.Image

import scala.scalajs.js.URIUtils

object Svg {
  /*
  def fromSvgContent(position: Rect, viewBox: Rect)(innerHtml: String): ReactTagOf[SVG] = <.svg.svg(
    ^.dangerouslySetInnerHtml(innerHtml),
    ^.svg.x := position.x, ^.svg.y := position.y, ^.svg.width := position.w, ^.svg.height := position.h,
    ^.svg.viewBox := s"${viewBox.x} ${viewBox.y} ${viewBox.w} ${viewBox.h}"
  )
  def fromSvgContent(width: Int, height: Int, viewBox: Rect)(innerHtml: String): ReactTagOf[SVG] = <.svg.svg(
    ^.dangerouslySetInnerHtml(innerHtml),
    ^.svg.width := width, ^.svg.height := height,
    ^.svg.viewBox := s"${viewBox.x} ${viewBox.y} ${viewBox.w} ${viewBox.h}"
  )
  */

  def uriEncodedSvgFromContent(svgContent: String, viewBox: Rect) = {
    import viewBox._
    val svg = s"<svg xmlns='http://www.w3.org/2000/svg' viewBox='$x $y $w $h'>$svgContent</svg>"
    //use import scalajs.js.JSStringOps.enableJSStringOps to enable jsIndexOf
    val uriEncodedSvg = URIUtils.encodeURIComponent(svg)
    s"data:image/svg+xml,$uriEncodedSvg"
  }

  def htmlImgFromSvgContent(svgContent: String, viewBox: Rect, tagMods: TagMod*): ReactTagOf[Image] = {
    val uriEncodedSvg = uriEncodedSvgFromContent(svgContent, viewBox)
    <.img(^.src := uriEncodedSvg, tagMods)
  }

  def svgImageFromSvgContent(position: Rect, viewBox: Rect, svgContent: String, tagMods: TagMod*) = {
    import position._
    val uriEncodedSvg = uriEncodedSvgFromContent(svgContent, viewBox)
    val mods = Seq(^.svg.x := x, ^.svg.y := y, ^.svg.width := w, ^.svg.height := h, ^.href := uriEncodedSvg) ++ tagMods
    <.svg.image.apply(mods:_*)
  }

  def viewBoxFromRect(rect: Rect, padding: Int) = {
    val minX = rect.x - padding
    val minY = rect.y - padding
    val width = rect.w + 2 * padding
    val height = rect.h + 2 * padding
    s"$minX $minY $width $height"
  }

  val moleculeSvgViewBox = Rect(0, 0, 400, 400)
  //val moleculeSvgViewBoxAttr = s"${moleculeSvgViewBox.x} ${moleculeSvgViewBox.y} ${moleculeSvgViewBox.w} ${moleculeSvgViewBox.h}"

}
