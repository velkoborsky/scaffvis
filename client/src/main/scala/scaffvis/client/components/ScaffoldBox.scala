package scaffvis.client.components

import scaffvis.ReusableComputation
import scaffvis.client.components.common.SvgProvider
import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions.{DeselectMolecules, SelectMolecules}
import scaffvis.client.store.model.{IndexedMolecules, Molecules, ViewState}
import scaffvis.shared.model.{Molecule, _}
import diode.data.{Pot, Ready}
import japgolly.scalajs.react._

object ScaffoldBox {

  case class Props(currentScaffold: Scaffold, scaffolds: Seq[Scaffold], svgProvider: SvgProvider,
                   molecules: Pot[Molecules],
                   tooltipControl: TooltipControl,
                   zoomOut: Callback,
                   zoomIn: ScaffoldId => Callback,
                   onWheelDownZoomOut: ReactWheelEvent => Callback,
                   viewState: ViewState
                  )

  def apply(props: Props) = component(props)

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      import props._

      val scaffoldsLvl = currentScaffold.level + 1

      val idToMolecule: MoleculeId => Option[Molecule] = molecules match {
        case Ready(ms) => ms.get
        case _ => _ => None
      }

      val activeScaffold: Option[ScaffoldId] = molecules.toOption.flatMap(_.active)
        .flatMap(idToMolecule)
        .flatMap(_.scaffoldId(scaffoldsLvl))

      val isAnyMoleculeSelected = molecules.exists(_.selected.nonEmpty)

      val selectedScaffolds: Set[ScaffoldId] = molecules.toIterable.flatMap(_.selected)
        .flatMap(idToMolecule(_))
        .flatMap(_.scaffoldId(scaffoldsLvl))
        .toSet

      val scaffoldMoleculesCount = getScaffoldMoleculesCountCached(molecules)

      val isActive: ScaPred = s => activeScaffold.contains(s.id)
      val isSelected: ScaPred = s => isAnyMoleculeSelected && selectedScaffolds.contains(s.id)
      val isSelectable: ScaPred = s => scaffoldMoleculesCount.exists(f => f(s) > 0)

      def scaffoldListBox = ScaffoldListBox(ScaffoldListBox.Props(
        scaffolds = scaffolds,
        isSelected = isSelected, isActive = isActive, isSelectable = isSelectable,
        scaffoldMoleculesCount = scaffoldMoleculesCount,
        svgProvider = svgProvider,
        toggleSelect = toggleSelect,
        zoomOut = zoomOut,
        zoomIn = zoomIn,
        isTopLevel = currentScaffold.level == HierarchyLevels.topLvl,
        sortOrder = viewState.scaffoldListSortOrder,
        tooltipControl = tooltipControl
      ))

      def scaffoldTreeMap = ScaffoldTreeMap(ScaffoldTreeMap.Props(
        scaffolds = scaffolds,
        isSelected = isSelected, isActive = isActive,
        scaffoldMoleculesCount = scaffoldMoleculesCount,
        svgProvider = svgProvider,
        isAnyMoleculeSelected = isAnyMoleculeSelected,
        tooltipControl = tooltipControl,
        toggleSelect = toggleSelect,
        zoomOut = zoomOut,
        zoomIn = zoomIn,
        onWheelDownZoomOut = props.onWheelDownZoomOut,
        viewState = viewState.scaffoldTreeMap
      ))

      if(viewState.showScaffoldsAsList) scaffoldListBox else scaffoldTreeMap
    }

    def getScaffoldMoleculesCount(molecules: Pot[Molecules]): Option[Scaffold => Int] =
      molecules.toOption.map(ms => (s: Scaffold) => ms.scaffoldMolecules(s.id).size)

    val getScaffoldMoleculesCountCached = {
      type I = Pot[Molecules]
      type K = IndexedMolecules
      type O = Option[Scaffold => Int]

      ReusableComputation[I, K, O](
        f = getScaffoldMoleculesCount,
        extractKey = _.map(_.im).getOrElse(null),
        eq = ReusableComputation.anyRefEq
      )
    }

    /**
      * none of descendants selected -> select all; else deselect all
      */
    val toggleSelect: ScaffoldId => ReactMouseEvent => Callback =
      scaffoldId => e => {
        $.props >>= { (props: Props) =>
          val selected = props.molecules.toIterable.flatMap(_.selected).toSet
          val descendants = props.molecules.toIterable.flatMap(_.scaffoldMolecules(scaffoldId)).toSet
          val isAnyDescendantSelected = selected.exists(descendants.contains)
          val action =
            if (isAnyDescendantSelected) DeselectMolecules(descendants)
            else SelectMolecules(descendants)
          Store.dispatchCB(action)
        }
      }
  }

  val component = ReactComponentB[Props]("ScaffoldBox")
    .renderBackend[Backend]
    .build

}