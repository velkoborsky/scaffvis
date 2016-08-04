package scaffvis.client.components

import scaffvis.client.AppMain.{ScaffoldLoc, Loc}
import scaffvis.client.components.common.{Svg, SvgProvider}
import scaffvis.client.store.model.Scaffolds
import scaffvis.layout.Rect
import scaffvis.shared.model.{HierarchyLevels, Scaffold, ScaffoldId}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.language.implicitConversions

object Breadcrumb {

  case class Props(proxy: ModelProxy[Scaffolds], router: RouterCtl[Loc], currentScaffoldId: ScaffoldId,
                   tooltipControl: TooltipControl)

  def apply(props: Props) = component(props)

  val totalHeight = 1000

  //divide: 90% items, 10% padding
  val itemSize: Double = totalHeight * 0.9 / HierarchyLevels.count
  val itemPadding: Double = totalHeight * 0.1 / Math.max(HierarchyLevels.count - 1, 1)

  val canvas = Rect(x = 0, y = 0, w = Math.ceil(itemSize), h = totalHeight)

  def itemY(level: Int) = (level - HierarchyLevels.topLvl) * (itemSize + itemPadding)

  val component = ReactComponentB[Props]("Breadcrumb")
    .render_P { case Props(proxy, router, currentScaffoldId, tooltipControl) => {
      val scaffolds = proxy.value
      val scaffoldTree = scaffolds.scaffoldHierarchy
      val svgProvider = scaffolds.svgProvider
      val currentScaffold = scaffoldTree(currentScaffoldId)
      val path = scaffoldTree.path(currentScaffold)

      val navigateTo: ScaffoldId => Callback = (sid) => router.set(ScaffoldLoc(sid))

      val triples = path.zipWithIndex.map {
        case (scaffold, idx) => {
          val position = Rect(x = 0, y = itemY(idx), w = itemSize, h = itemSize)
          renderScaffold(scaffold = scaffold, svgProvider = svgProvider, position = position,
            isCurrent = (scaffold == currentScaffold), navigateTo = navigateTo, tooltipControl = tooltipControl
          )
        }
      }
      val threeSeqs = triples.unzip3
      val seq = threeSeqs match { case (bgrs, isvgs, fgrs) => bgrs ++ isvgs.flatten ++ fgrs }

      <.div(
        ^.id := "Breadcrumb", ^.className := "breadcrumb",
        <.svg.svg(
          ^.width := "100%", ^.height := "100%",
          ^.svg.viewBox := Svg.viewBoxFromRect(canvas, padding = 5),
          seq.toReactNodeArray
        )
      )
    }}
    .componentDidMount(scope => onComponentRenderedLoadMissingSVG(scope.props))
    .componentDidUpdate(scope => onComponentRenderedLoadMissingSVG(scope.currentProps))
    .build

  implicit def positionTagMod(r: Rect): TagMod =
    Seq(^.svg.x := r.x, ^.svg.y := r.y, ^.svg.width := r.w, ^.svg.height := r.h)

  private def renderScaffold(scaffold: Scaffold, svgProvider: SvgProvider, position: Rect, isCurrent: Boolean,
                             navigateTo: ScaffoldId => Callback, tooltipControl: TooltipControl
                            ) = {
    // three layers:
    //  1) rect with bg color
    //  2) svg
    //  3) transparent rect with mouse events

    val classNameBase: String = {
      val sb = new StringBuilder
      sb.append("breadcrumb-molecule")
      if (isCurrent)
        sb.append(" current")
      sb.toString()
    }

    val backgroundRect = <.svg.rect(
      ^.className := s"breadcrumb-molecule-bg $classNameBase", ^.key := scaffold.id * 10 + 1, position
    )

    val foregroundRect = <.svg.rect(
      ^.className := s"breadcrumb-molecule-fg $classNameBase", ^.key := scaffold.id * 10 + 2, position,
      ^.onMouseOver ==> tooltipControl.showTooltipScaffold(scaffold),
      ^.onMouseMove ==> tooltipControl.moveTooltip,
      ^.onMouseOut --> tooltipControl.hideTooltip,
      ^.onClick --> navigateTo(scaffold.id)
    )

    val innerSvg = svgProvider.getSvg(scaffold.id).map(svgContent =>
      Svg.svgImageFromSvgContent(position, Svg.moleculeSvgViewBox, svgContent, ^.key := scaffold.id * 10 + 3)
    )

    (backgroundRect, innerSvg, foregroundRect)
  }

  /**
    * loads missing SVGs
    */
  val onComponentRenderedLoadMissingSVG: Props => Callback =
    (props) => props.proxy.value.svgProvider.loadMissingSvgCallback()

}
