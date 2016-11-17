package scaffvis.client.components

import scaffvis.client.components.common.{GlyphIcon, ReusableCmps}
import scaffvis.client.store.model.Model
import scaffvis.shared.model.Scaffold
import diode.data.{Failed, Pending, Ready}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object Footer {

  case class Props(model: Model, currentScaffold: Scaffold)

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props) = {
      import props._



      <.div(^.className := "footer",
        <.div(^.className := "footer-text",
          model.molecules match {
            case Ready(molecules) => {
              val datasetSize = molecules.molecules.size
              val datasetSelectionSize = if(molecules.selected.isEmpty) None else Some(molecules.selected.size)
              val subtreeSize = molecules.scaffoldMolecules(currentScaffold.id).size
              val subtreeSelection = ReusableCmps.selectedMoleculesInSubtree(molecules, currentScaffold)
              val subtreeSelectionSize = if(subtreeSelection.isEmpty) None else Some(subtreeSelection.size)

              s"Dataset: $subtreeSize molecules in current subtree" +
                subtreeSelectionSize.map(n => s" ($n selected)").getOrElse("") +
                s", $datasetSize molecules total" +
                datasetSelectionSize.map(n => s" ($n selected)").getOrElse("")
            }
            case Pending(_) => Seq[ReactNode]("Loading dataset ", GlyphIcon.refresh)

            case Failed(e) => Seq[ReactNode](
              <.span(^.color := "red", GlyphIcon.exclamationSign),
              s" Loading dataset failed. You might be trying to use an unsupported file format. Error: ${e.getMessage}"
            )

            case _ => "No dataset loaded."
          }
        ),
        <.div(^.className := "footer-text", ^.float := "right",
          <.a(^.href := "https://github.com/velkoborsky/scaffvis/issues", "Report a problem")
        )
      )

    }
  }

  val component = ReactComponentB[Props]("Footer")
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
