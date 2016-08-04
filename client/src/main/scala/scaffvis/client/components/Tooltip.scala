package scaffvis.client.components

import scaffvis.client.components.common.{Formatter, Svg, SvgProvider}
import scaffvis.client.store.model.{Molecules, Scaffolds}
import scaffvis.shared.model._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLDivElement

import scala.scalajs.js.UndefOr

object Tooltip {

  case class Props(scaffolds: Scaffolds,
                   molecules: Pot[Molecules]) {
    lazy val moleculesSvgProvider = molecules.toOption.map(SvgProvider(_))
  }

  def apply(ref: RefComp[Props, Tooltip.State, Tooltip.Backend, TopNode])(props: Props) =
    component.withRef(ref)(props)

  case class State(content: TooltipContent = TooltipContent.EmptyTC)

  class Backend($: BackendScope[Props, State]) {

    def render(props: Props, state: State) = {
      <.div(^.id := "MainTooltip", ^.className := "maintooltip", ^.ref := selfRef,
        ^.display := (state.content match {
          case TooltipContent.EmptyTC => "none"
          case _ => "block"
        }),
        state.content match {
          case TooltipContent.ScaffoldTC(s) =>
            renderScaffoldTooltip(s, props.scaffolds.svgProvider, props.molecules)
          case TooltipContent.MoleculeTC(m) =>
            renderMoleculeTooltip(m, props.moleculesSvgProvider)
          case TooltipContent.ImageTC(svg) =>
            renderImage(svg)
          case TooltipContent.EmptyTC =>
            EmptyTag
        }
      )
    }

    def renderScaffoldTooltip(scaffold: Scaffold, svgProvider: SvgProvider, molecules: Pot[Molecules]) = {
      lazy val scaffoldMoleculesCount = getScaffoldMoleculesCount(molecules)
      renderImageHeadingLines(
        svgContent = svgProvider.getSvg(scaffold.id).getOrElse(""),
        heading = Formatter.scaffoldHeading(scaffold),
        lines = Formatter.scaffoldLines(scaffoldMoleculesCount)(scaffold)
      )
    }

    def renderMoleculeTooltip(molecule: Molecule, svgProvider: Option[SvgProvider]) = {
      renderImageHeadingLines(
        svgContent = svgProvider.flatMap(svgp => svgp.getSvg(molecule.id)).getOrElse(""),
        heading = Formatter.moleculeHeading(molecule),
        lines = Formatter.moleculeLines(molecule)
      )
    }

    def renderImageHeadingLines(svgContent: String, heading: String, lines: Seq[String]) = {
      <.div(^.display := "table", ^.width := "52.36em",
        <.div(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px", ^.width := "1%",
          Svg.htmlImgFromSvgContent(
            svgContent,
            Svg.moleculeSvgViewBox,
            ^.width := "20em", ^.height := "20em"
          )
        ),
        <.div(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px 6px 0px",
          <.span(^.className := "list-group-item-heading",
            <.p(<.strong(heading)),
            lines.map(l => <.p(l))
          )
        )
      )
    }

    def renderImage(svgContent: String) = {
      Svg.htmlImgFromSvgContent(
        svgContent,
        Svg.moleculeSvgViewBox,
        ^.width := "20em", ^.height := "20em", ^.padding := "6px 12px"
      )
    }

    def getScaffoldMoleculesCount(molecules: Pot[Molecules]): Option[Scaffold => Int] =
      molecules.toOption.map(ms => (s: Scaffold) => ms.scaffoldMolecules(s.id).size)

    val selfRef: RefSimple[HTMLDivElement] = Ref("self")

    def move(x: Double, y: Double): Unit =
      selfRef($).foreach { self =>
        val height = self.offsetHeight
        val windowHeight = org.scalajs.dom.window.innerHeight
        val maxY = windowHeight - height
        self.style.top = Math.min(y, maxY).px
        self.style.left = x.px
      }

    def moveToPageEvent(e: ReactMouseEvent): Unit = {
      move(e.pageX + 50, e.pageY - 30)
    }
  }

  /**
    * loads missing SVGs
    */
  private val onComponentRendered: Props => Callback =
    (props) => props.scaffolds.svgProvider.loadMissingSvgCallback() >>
      props.moleculesSvgProvider.map(_.loadMissingSvgCallback()).getOrElse(Callback.empty)

  val component = ReactComponentB[Props]("Tooltip")
    .initialState[State](State())
    .renderBackend[Backend]
    .componentDidMount(scope => onComponentRendered(scope.props))
    .componentDidUpdate(scope => onComponentRendered(scope.$.props))
    .build

}

sealed trait TooltipContent

object TooltipContent {
  case class ScaffoldTC(scaffold: scaffvis.shared.model.Scaffold) extends TooltipContent
  case class MoleculeTC(molecule: scaffvis.shared.model.Molecule) extends TooltipContent
  case class ImageTC(svg: String) extends TooltipContent
  case object EmptyTC extends TooltipContent
}

case class TooltipControl(getTooltip: () => UndefOr[ReactComponentM[Tooltip.Props, Tooltip.State, Tooltip.Backend, TopNode]]) {

  val showTooltip: TooltipContent => ReactMouseEvent => Callback =
    content => e => Callback {
      getTooltip().foreach{tt =>
        tt.backend.moveToPageEvent(e)
        tt.setState(Tooltip.State(content = content))
      }
    }

  val showTooltipScaffold: Scaffold => ReactMouseEvent => Callback =
    scaffold => showTooltip(TooltipContent.ScaffoldTC(scaffold))

  val showTooltipMolecule: Molecule => ReactMouseEvent => Callback =
    molecule => showTooltip(TooltipContent.MoleculeTC(molecule))

  val showTooltipImageOpt: Option[String] => ReactMouseEvent => Callback =
    imageOpt => showTooltip(imageOpt.map(TooltipContent.ImageTC).getOrElse(TooltipContent.EmptyTC))

  val moveTooltip: ReactMouseEvent => Callback =
    e => Callback(getTooltip().foreach(_.backend.moveToPageEvent(e)))

  val hideTooltip: Callback =
    Callback(getTooltip().foreach(_.setState(Tooltip.State())))

}