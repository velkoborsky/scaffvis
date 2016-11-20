package scaffvis.client.components

import scaffvis.ReusableComputation
import scaffvis.client.components.common.{CSS, Svg, SvgProvider}
import scaffvis.client.store.model.VSScaffoldTreemap
import scaffvis.layout.{Color, Rect, TreemapLayout}
import scaffvis.shared.model._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.collection.immutable.HashMap
import scala.language.implicitConversions
import scalacss.ScalaCssReact._

object ScaffoldTreeMap {

  case class Props(
                    scaffolds: Seq[Scaffold],
                    isSelected: ScaPred, isActive: ScaPred,
                    scaffoldMoleculesCount: Option[Scaffold => Int],
                    svgProvider: SvgProvider,
                    isAnyMoleculeSelected: Boolean,
                    tooltipControl: TooltipControl,
                    toggleSelect: ScaffoldId => ReactMouseEvent => Callback,
                    zoomOut: Callback,
                    zoomIn: ScaffoldId => Callback,
                    onWheelDownZoomOut: ReactWheelEvent => Callback,
                    viewState: VSScaffoldTreemap
                  )

  def apply(props: Props) = component(props)

  val svgMinSize = 20 //min(w,h) in ‰

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      import props._

      //calculate the layout
      val layout: Map[ScaffoldId, Rect] = {
        import viewState._
        getLayoutCached(scaffolds, scaffoldMoleculesCount, sizeSelectFunction, sizeTransformationFunction)
      }

      implicit def positionTagMod(r: Rect): TagMod =
        Seq(^.svg.x := r.x, ^.svg.y := r.y, ^.svg.width := r.w, ^.svg.height := r.h)

      //calculate colors
      val colors: Seq[Color] = {
        import viewState._
        getColorsCached(scaffolds, scaffoldMoleculesCount,
          colorSelectFunction, colorTransformationFunction, colorGradientFunction)
      }

      //create elements
      val backgroundRects = scaffolds.zip(colors).flatMap { case (scaffold, color) =>
        layout.get(scaffold.id).map { position =>
          <.svg.rect(
            ^.key := scaffold.id * 10 + 1, position,
            CSS.treemapMoleculeBg, //^.className := "treemap-molecule-bg",
            ^.svg.fill := color.css
          )
        }
      }

      val foregroundRects = scaffolds.flatMap { scaffold =>
        import tooltipControl._
        layout.get(scaffold.id).map { position =>
          <.svg.rect(
            ^.key := scaffold.id * 10 + 2, position,
            CSS.treemapMoleculeFg, //^.className := s"treemap-molecule-fg $classNameBase",
            isActive(scaffold) ?= CSS.active,
            (isAnyMoleculeSelected && !isSelected(scaffold)) ?= CSS.deselected,
            ^.onMouseOver ==> showTooltipScaffold(scaffold),
            ^.onMouseMove ==> moveTooltip,
            ^.onMouseOut --> hideTooltip,
            ^.onWheel ==> onWheelNavigation(scaffold.id),
            ^.onDblClick ==> onNonShiftDblClick(zoomIn(scaffold.id)),
            ^.onClick ==> onShiftClick(toggleSelect(scaffold.id))
          )
        }
      }

      val images = scaffolds.flatMap { scaffold =>
        import scaffold.id
        layout.get(id).filter(_.minSize >= svgMinSize).flatMap { position =>
          svgProvider.getSvg(id).map(svgContent =>
            Svg.svgImageFromSvgContent(position, Svg.moleculeSvgViewBox, svgContent, ^.key := id * 10 + 3)
          )
        }
      }

      <.svg.svg(LeftBox.canvasCommonTagMods("ScaffoldTreeMap"),
        ^.onWheel ==> onWheelDownZoomOut,
        backgroundRects.toReactNodeArray,
        images.toReactNodeArray,
        foregroundRects.toReactNodeArray
      )
    }

    val onWheelNavigation: ScaffoldId => ReactWheelEvent => Callback =
      scaffoldId => e => {
        e.preventDefault()
        $.props >>= { (props: Props) =>
          if (e.deltaY < 0) //scroll up ~ zoom
            props.zoomIn(scaffoldId)
          else
            props.zoomOut
        }
      }

    def onShiftClick(cb: => ReactMouseEvent => Callback): ReactMouseEvent => Callback = {
      e: ReactMouseEvent =>
        if(e.getModifierState("Shift")) cb(e)
        else Callback.empty
    }

    def onNonShiftDblClick(cb: => Callback): ReactMouseEvent => Callback = {
      e: ReactMouseEvent =>
        if(! e.getModifierState("Shift")) cb
        else Callback.empty
    }

    def getLayout(scaffolds: Seq[Scaffold], scaffoldMoleculesCount: Option[Scaffold => Int],
                  sizeSelectFunction: SizeSelectFunction,
                  sizeTransformationFunction: TransformationFunction
              ): Map[ScaffoldId, Rect] = {
      //1. select
      lazy val counts = scaffoldMoleculesCount match {
        case None => scaffolds.map(_ => 1)
        case Some(f) => scaffolds.map(f)
      }
      val values: Seq[Double] = {
        import SizeSelectFunction._
        sizeSelectFunction match {
          case PubchemCounts => scaffolds.map(s => s.subtreeSize.toDouble)
          case DatasetCounts => counts.map(c => c.toDouble)
        }
      }

      //2. transform
      val transformed: Seq[Double] = values.map(sizeTransformationFunction)

      //3. create input
      val layoutInput = for{
        (s,v) <- scaffolds.zip(transformed)
        if v > 0
      } yield TreemapLayout.InputEntry(s.id, v)

      //4. calculate
      val layoutOutput = TreemapLayout.layout(entries = layoutInput, canvas = LeftBox.canvas)

      //5. transform output
      val rects: HashMap[ScaffoldId, Rect] = layoutOutput.map(x => x.id -> x.rect)(collection.breakOut)
      rects
    }
    val getLayoutCached = Function untupled
      ReusableComputation.simpleE((getLayout _).tupled, ReusableComputation.productMembersEq)

    def getColors(scaffolds: Seq[Scaffold], scaffoldMoleculesCount: Option[Scaffold => Int],
                  colorSelectFunction: ColorSelectFunction,
                  colorTransformationFunction: TransformationFunction,
                  colorGradientFunction: ColorGradientFunction
                 ): Seq[Color] = {
      //1. select
      lazy val counts = scaffoldMoleculesCount match {
        case None => scaffolds.map(_ => 0)
        case Some(f) => scaffolds.map(f)
      }

      val values: Seq[Double] = {
        import ColorSelectFunction._
        colorSelectFunction match {
          case DatasetCounts =>
            counts.map(c => c.toDouble)
          case DatasetDivPubchemCounts =>
            scaffolds.zip(counts).map{case(s,c) => c.toDouble / s.subtreeSize}
          case PubchemCounts =>
            scaffolds.map(s => s.subtreeSize.toDouble)
        }
      }

      //2. transform
      val transformed: Seq[Double] = values.map(colorTransformationFunction)

      //3. normalize
      val normalized: Seq[Double] = normalize(transformed.toList)

      //4. colorize
      val colors: Seq[Color] = normalized.map(colorGradientFunction)
      colors
    }
    val getColorsCached = Function untupled
      ReusableComputation.simpleE((getColors _).tupled, ReusableComputation.productMembersEq)

    def normalize(xs: Seq[Double]) = {
      val max = xs.max
      if(max > 0)
        xs.view.map(_/max)
      else
        xs
    }

  }

  sealed trait ColorSelectFunction {
    def name: String
  }
  object ColorSelectFunction {
    case object DatasetCounts extends ColorSelectFunction {
      override val name = "Dataset counts"
    }
    case object DatasetDivPubchemCounts extends ColorSelectFunction {
      override val name = "Dataset counts relative to Pubchem Compounds counts"
    }
    case object PubchemCounts extends ColorSelectFunction {
      override val name = "Pubchem Compounds counts - disregarding dataset"
    }

    def default = DatasetCounts
    def values = Seq[ColorSelectFunction](DatasetCounts, DatasetDivPubchemCounts, PubchemCounts)
  }

  sealed trait TransformationFunction extends (Double => Double) {
    def name: String
  }
  object TransformationFunction {
    case object Identity extends TransformationFunction {
      val name = "None"
      override def apply(x: Double) = x
    }
    case object Logarithmic extends TransformationFunction {
      val name = "Logaritmic: ln(1+x)"
      override def apply(x: Double) = Math.log1p(x)
    }
    def default = Identity
    def values = Seq[TransformationFunction](Identity, Logarithmic)
  }

  type ColorGradientFunction = Double => Color

  sealed trait SizeSelectFunction {
    def name: String
  }
  object SizeSelectFunction {
    case object PubchemCounts extends SizeSelectFunction{
      override val name = "Pubchem counts"
    }
    case object DatasetCounts extends SizeSelectFunction {
      override val name = "Dataset counts"
    }
    def default = PubchemCounts
    def values = Seq[SizeSelectFunction](PubchemCounts, DatasetCounts)
  }


  /**
    * loads missing SVGs
    */
  val onComponentRendered: Props => Callback =
    (props) => props.svgProvider.loadMissingSvgCallback(limit = 50)

  val component = ReactComponentB[Props]("ScaffoldTreeMap")
    .renderBackend[Backend]
    .componentDidMount(scope => onComponentRendered(scope.props))
    .componentDidUpdate(scope => onComponentRendered(scope.currentProps))
    .build

}