package scaffvis.client.components

import scaffvis.client.components.common.{CSS, Svg, SvgProvider}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions.{ActivateMolecule, DeactivateMolecule, DeselectMolecule, SelectMolecule}
import scaffvis.client.store.model.Molecules
import scaffvis.layout.Rect
import scaffvis.shared.model.{Molecule, _}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactTagOf
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.svg._

import scala.collection.immutable.IndexedSeq
import scala.language.implicitConversions
import scalacss.ScalaCssReact._

object MoleculeGallery {

  case class Props(currentScaffold: Scaffold, molecules: Molecules,
                   tooltipControl: TooltipControl,
                   onWheelDownZoomOut: ReactWheelEvent => Callback
                  ) {
    val svgProvider = SvgProvider(molecules) //our own provider - so that we can use limited requests
  }

  def apply(props: Props) = component(props)

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      import props._

      val ms: IndexedSeq[Molecule] = molecules
        .scaffoldMolecules(currentScaffold.id)
        .map(molecules.get_!)
        .toIndexedSeq

      if(ms.nonEmpty) {
        <.svg.svg(LeftBox.canvasCommonTagMods("MoleculeGallery"),
          ^.onWheel ==> onWheelDownZoomOut,
          renderMolecules(ms, props).toReactNodeArray
        )
      } else {
        <.div(CSS.centeredBoxDefault, "No matching molecules in dataset")
      }
    }

     def renderMolecules(ms: IndexedSeq[Molecule], props: Props) = {
       import props.molecules
       //calculate the layout
       val rects: Seq[Rect] = layout(ms.size)
       val svgProvider = props.svgProvider

       val isSelected = getIsSelected(molecules.selected)
       val isAnyMoleculeSelected = molecules.selected.nonEmpty

       val triples: Seq[(ReactTagOf[RectElement], Option[ReactTagOf[Image]], ReactTagOf[RectElement])] =
         ms.zip(rects)
           .map { case (molecule, rect) =>
             import molecule.id
             renderMolecule(molecule = molecule, svg = svgProvider.getSvg(molecule.id),
               position = rect,
               isActive = molecules.active.contains(id),
               isAnyMoleculeSelected = isAnyMoleculeSelected,
               isSelected = isSelected(molecule),
               props = props
             )
           }
       val threeSeqs = triples.unzip3
       val seq = threeSeqs match { case (bgrs, isvgs, fgrs) => bgrs ++ isvgs.flatten ++ fgrs }
       seq
     }

    def layout(size: Int): Seq[Rect] = {
      val columns = Math.ceil(Math.sqrt(size)).toInt

      val totalSize = LeftBox.canvas.minSize
      val itemSize: Double = totalSize / columns

      for {
        i <- 0 until size
        col: Int = i % columns
        row: Int = i / columns
      } yield Rect(
        x = col * itemSize,
        y = row * itemSize,
        w = itemSize,
        h = itemSize
      )
    }

    implicit def positionTagMod(r: Rect): TagMod =
      Seq(^.svg.x := r.x, ^.svg.y := r.y, ^.svg.width := r.w, ^.svg.height := r.h)

    private def renderMolecule(molecule: Molecule, svg: Option[String], position: Rect,
                               isActive: Boolean, isSelected: Boolean, isAnyMoleculeSelected: Boolean,
                               props: Props
                              ) = {
      // three layers: 1) rect with bg color + white stroke,2) svg,
      // 3) rect with mouse events + transparent bg (used for disabling) + transparent stroke (used for activation)

      val isDeselected = isAnyMoleculeSelected && ! isSelected

      val classNameBase: String = {
        val sb = new StringBuilder
        sb.append("moleculemap-molecule")
        if(isActive)
          sb.append(" active")
        if(isDeselected)
          sb.append(" deselected")
        sb.toString()
      }

      val backgroundRect = <.svg.rect(
        ^.className := s"moleculemap-molecule-bg $classNameBase", ^.key := molecule.id * 10 + 1, position
      )

      def activateMoleculeAndShowTooltip(molecule: Molecule): ReactMouseEvent => Callback =
        e => activateMolecule(molecule.id) >> props.tooltipControl.showTooltipMolecule(molecule)(e)
      val deactivateMoleculeAndHideTooltip: Callback =
        deactivateMolecule >> props.tooltipControl.hideTooltip

      val foregroundRect = <.svg.rect(
        ^.className := s"moleculemap-molecule-fg $classNameBase", ^.key := molecule.id * 10 + 2, position,
        ^.onMouseOver ==> activateMoleculeAndShowTooltip(molecule),
        ^.onMouseMove ==> props.tooltipControl.moveTooltip,
        ^.onMouseOut --> deactivateMoleculeAndHideTooltip,
        ^.onWheel ==> props.onWheelDownZoomOut,
        ^.onClick --> toggleSelected(isSelected, molecule.id)
      )

      val innerSvg = svg.map(svgContent =>
        Svg.svgImageFromSvgContent(position, Svg.moleculeSvgViewBox, svgContent, ^.key := molecule.id * 10 + 3)
      )

      (backgroundRect, innerSvg, foregroundRect)
    }

    val activateMolecule: (MoleculeId) => Callback = (id: Int) => Store.dispatchCB(ActivateMolecule(id))
    val deactivateMolecule: Callback = Store.dispatchCB(DeactivateMolecule)

    val toggleSelected: (Boolean, MoleculeId) => Callback = (isSelected, id) => {
      Store.dispatchCB(
        if (isSelected) DeselectMolecule(id)
        else SelectMolecule(id)
      )
    }

    def getIsSelected(selected: Set[MoleculeId]): MolPred = m => selected.contains(m.id)

  }

  /**
    * loads missing SVGs
    */
  val onComponentRenderedLoadMissingSVG: Props => Callback =
    (props) => props.svgProvider.loadMissingSvgCallback(limit = 20)

  val component = ReactComponentB[Props]("MoleculeGallery")
    .renderBackend[Backend]
    .componentDidMount(scope => onComponentRenderedLoadMissingSVG(scope.props))
    .componentDidUpdate(scope => onComponentRenderedLoadMissingSVG(scope.currentProps))
    .build

}
