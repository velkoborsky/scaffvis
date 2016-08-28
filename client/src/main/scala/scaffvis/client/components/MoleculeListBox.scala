package scaffvis.client.components

import diode.data.{Empty, Failed, Pending, Ready}
import japgolly.scalajs.react._
import scaffvis.client.components.common.{Formatter, SvgProvider}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions._
import scaffvis.client.store.model.{Model, VSFilter}
import scaffvis.shared.model.{Molecule, MoleculeId, Scaffold}

object MoleculeListBox {

  case class Props(model: Model, currentScaffold: Scaffold, tooltipControl: TooltipControl)

  def apply(props: Props) = component(props)

  val MoleculesListBox = ListBox.component[Molecule]

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {

      import props._

      val items = model.molecules match {
        case Empty => Left("No dataset loaded")
        case Failed(_) => Left("Loading dataset failed")
        case Pending(_) => Left("Loading dataset")
        case Ready(molecules) => {

          val filter = model.viewState.filter

          val isInSubtree = getIsInSubtree(currentScaffold)
          val isSelected = getIsSelected(molecules.selected)
          val isSearchResultOrNS = getISearchResultOrNoSearch(filter.search)

          val isEnabled: MolPred = m => isInSubtree(m) && isSearchResultOrNS(m)
          val isActive: MolPred = m => molecules.active.contains(m.id)

          val ms = moleculesIterable(molecules.im.molecules, filter,
            isInSubtree = isInSubtree,
            isSelected = isSelected,
            isSearchResultOrNoSearch = isSearchResultOrNS)

          if (ms.nonEmpty) {
            Right(ListBox.Items(
              items = ms,
              id = itemId, heading = Formatter.moleculeHeading, lines = Formatter.moleculeLines,
              isEnabled = isEnabled, isActive = isActive, isSelected = isSelected,
              svgProvider = SvgProvider(molecules),
              onClick = toggleSelected, onMouseEnter = activateMolecule, onMouseLeave = deactivateMolecule,
              toolbarButtons = Seq(
                ListBox.Button("Deselect All", onClick = deselectAll, enabled = molecules.selected.nonEmpty)
              )
            ))
          } else {
            Left("No matching molecules in dataset")
          }
        }
        case _ => Left("Unexpected state")
      }

      MoleculesListBox(ListBox.Props(htmlId = "MoleculesBox", items = items, tooltipControl = tooltipControl))
    }

    val itemId: Molecule => Int = m => m.id

    val toggleSelected: (Boolean, MoleculeId) => ReactMouseEvent => Callback = (isSelected, id) => e =>
      Store.dispatchCB(
        if (isSelected) DeselectMolecule(id)
        else SelectMolecule(id)
      )
    val deselectAll: Callback = Store.dispatchCB(DeselectAllMolecules)

    val activateMolecule: (MoleculeId) => Callback = (id: Int) => Store.dispatchCB(ActivateMolecule(id))
    val deactivateMolecule: (MoleculeId) => Callback = _ => Store.dispatchCB(DeactivateMolecule)

    def getIsInSubtree(currentScaffold: Scaffold): MolPred = m => m.isInSubtree(currentScaffold)
    def getIsSelected(selected: Set[MoleculeId]): MolPred = m => selected.contains(m.id)
    def getISearchResultOrNoSearch(search: Option[String]): MolPred = m => search match {
      case None => true
      case Some(s) => m.smiles.contains(s) || m.name.exists(name => name.contains(s))
    }

    def moleculesIterable(molecules: Vector[Molecule], filter: VSFilter,
                          isInSubtree: MolPred, isSelected: MolPred, isSearchResultOrNoSearch: MolPred
                         ) = {
      val m = molecules.view
      val n = if(filter.showOnlySubtree) m.withFilter(isInSubtree) else m
      val o = if(filter.showOnlySelected) m.withFilter(isSelected) else n
      val p = if(filter.showOnlySearchResults) o.withFilter(isSearchResultOrNoSearch) else o
      p
    }
  }

  val component = ReactComponentB[Props]("MoleculeListBox")
    .renderBackend[Backend]
    .build

}


