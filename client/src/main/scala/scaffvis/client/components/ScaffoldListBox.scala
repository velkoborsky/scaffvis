package scaffvis.client.components

import scaffvis.ReusableComputation
import scaffvis.client.components.common.{Formatter, SvgProvider}
import scaffvis.shared.model.{Scaffold, ScaffoldId}
import japgolly.scalajs.react._

object ScaffoldListBox {

  case class Props(
                    scaffolds: Seq[Scaffold],
                    isSelected: ScaPred, isActive: ScaPred, isSelectable: ScaPred,
                    scaffoldMoleculesCount: Option[Scaffold => Int],
                    svgProvider: SvgProvider,
                    toggleSelect: ScaffoldId => ReactMouseEvent => Callback,
                    zoomOut: Callback,
                    zoomIn: ScaffoldId => Callback,
                    isTopLevel: Boolean,
                    sortOrder: SortOrder,
                    tooltipControl: TooltipControl
                  )


  val ScaffoldListBox = ListBox.component[Scaffold]

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {

      import props._

      val lbItems = ListBox.Items(
        items = sortScaffoldsCached(scaffolds, scaffoldMoleculesCount, sortOrder),
        id = itemId, heading = Formatter.scaffoldHeading, lines = Formatter.scaffoldLines(scaffoldMoleculesCount),
        isSelected = isSelected,
        isActive = isActive,
        isSelectable = isSelectable,
        svgProvider = svgProvider,
        onClick = (_, id) => toggleSelect(id),
        onMouseEnter = intToEmptyCB,
        onMouseLeave = intToEmptyCB,
        itemButton = Some(ListBoxItem.Button("Zoom In", onClick = zoomIn)),
        toolbarButtons = Seq(ListBox.Button("Zoom Out", onClick = zoomOut, enabled = !isTopLevel))
      )

      ScaffoldListBox(ListBox.Props(htmlId = "ScaffoldListBox", items = Right(lbItems),
        tooltipControl = tooltipControl))
    }

    def sortScaffolds(scaffolds: Seq[Scaffold], scaffoldMoleculesCount: Option[Scaffold => Int],
                      sortOrder: SortOrder
                     ): Seq[Scaffold] = {
      import SortOrder._
      sortOrder match {
        case Lexicographic => scaffolds
        case PubchemCounts => scaffolds.sortWith(lt = (s1, s2) => s1.subtreeSize > s2.subtreeSize)
        case DatasetCounts => scaffoldMoleculesCount match {
          case None =>
            println("Unable to use Dataset count sort order, using default")
            scaffolds
          case Some(f) =>
            scaffolds.sortWith(lt = (s1, s2) => f(s1) > f(s2))
        }
      }
    }
    val sortScaffoldsCached = Function untupled
      ReusableComputation.simpleE((sortScaffolds _).tupled, ReusableComputation.productMembersEq)
  }

  val itemId: Scaffold => Int = s => s.id

  val intToEmptyCB: (Int) => Callback = (_) => Callback.empty

  val component = ReactComponentB[Props]("ScaffoldListBox")
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component.apply(props)


  //configuration parameters
  sealed trait SortOrder {
    def name: String
  }
  object SortOrder {
    case object Lexicographic extends SortOrder {
      val name = "Lexicographic"
    }
    case object PubchemCounts extends SortOrder {
      val name = "Pubchem Counts"
    }
    case object DatasetCounts extends SortOrder {
      val name = "Dataset Counts"
    }
    def default = Lexicographic
    def values = Seq[SortOrder](Lexicographic, DatasetCounts, PubchemCounts)
  }

}
