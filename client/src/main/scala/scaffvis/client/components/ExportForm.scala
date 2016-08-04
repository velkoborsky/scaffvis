package scaffvis.client.components

import scaffvis.client.components.common.{CSS, GlyphIcon}
import scaffvis.client.store.model.Model
import scaffvis.client.store.serializer.BooPickleSerializer
import scaffvis.client.util.{DataUrl, DomHelpers}
import scaffvis.shared.model.{Molecule, ScaffoldId, SmilesScaffold}
import diode.data._
import diode.react.ModelProxy
import japgolly.scalajs.react.CompScope.DuringCallbackM
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, _}

import scala.util.{Failure, Success, Try}
import scalacss.ScalaCssReact._


object ExportForm {

  case class Props(submitHandler: () => Callback, proxy: ModelProxy[Model], currentScaffoldId: Option[Int])

  sealed trait ExportType
  case object Dataset extends ExportType
  case object Molecules extends ExportType
  case object Scaffolds extends ExportType

  sealed trait Data {
    def fileName: String
  }
  case class StringData(string: String, fileName: String) extends Data
  case class ByteArrayData(bytes: Array[Byte], fileName: String) extends Data

  case class State(
                    selectedType: Option[ExportType] = None,
                    datasetSelection: Boolean = true,
                    moleculesOnlySelected: Boolean = true,
                    moleculesOnlySubtree: Boolean = true,
                    moleculesOnlySearchResults: Boolean = false,
                    data: Pot[Data] = Empty)

  //def isGenerating(s: State) = s.selectedType.nonEmpty && s.data.isEmpty
  //def dataReady(s: State) = s.data.nonEmpty

  class Backend($: BackendScope[Props, State]) {

    def formClosed(state: State, props: Props): Callback = props.submitHandler()

    def render(p: Props, s: State) = {

      def activeFor(et: ExportType) = s.selectedType.contains(et) ?= CSS.active
      def onClickSetET(et: ExportType) = ^.onClick --> $.modState(_.copy(selectedType = Some(et), data = Empty))

      def stopPropModState(f: State => State): ReactEventI => Callback =
        e => {
          e.stopPropagation()
          $.modState(f)
        }

      def onCheckboxChange(f: State => State) =
        ^.onChange ==> stopPropModState(f)

      BootstrapModal(BootstrapModal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, CSS.close, ^.onClick --> hide, GlyphIcon.remove), <.h4("Export Data")),
        // footer button
        footer = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, ^.onClick --> hide, "Close")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.h5("Select data to export"),
        <.div(CSS.listGroup,

          //Dataset box
          <.a(CSS.listGroupItem, activeFor(Dataset), onClickSetET(Dataset),
            <.h4("Dataset"),
            "Export the current dataset in a custom binary format which includes scaffold information and speeds up loading of the dataset by orders of magnitude. Optionally includes the list of selected molecules.",
            <.form(
              <.div(^.className := "checkbox",
                <.label(
                  <.input(^.`type` := "checkbox",
                    ^.checked := s.datasetSelection,
                    onCheckboxChange(_.copy(selectedType = Some(Dataset), data = Empty,
                      datasetSelection = !s.datasetSelection))
                  ),
                  "Save selection information"
                )
              )
            )
          ),

          //Molecules box
          <.a(CSS.listGroupItem, activeFor(Molecules), onClickSetET(Molecules),
            <.h4("Molecules"),
            "Export a subset of current dataset molecules in a standard SMILES file format.",
            <.form(
              <.div(^.className := "checkbox",
                <.label(
                  <.input(^.`type` := "checkbox",
                    ^.checked := s.moleculesOnlySubtree,
                    onCheckboxChange(_.copy(selectedType = Some(Molecules), data = Empty,
                      moleculesOnlySubtree = !s.moleculesOnlySubtree))
                  ),
                  "Only molecules in current subtree"
                )
              ),
              <.div(^.className := "checkbox",
                <.label(
                  <.input(^.`type` := "checkbox",
                    ^.checked := s.moleculesOnlySelected,
                    onCheckboxChange(_.copy(selectedType = Some(Molecules), data = Empty,
                      moleculesOnlySelected = !s.moleculesOnlySelected))
                  ),
                  "Only selected molecules"
                )
              ),
              <.div(^.className := "checkbox",
                <.label(
                  <.input(^.`type` := "checkbox",
                    ^.checked := s.moleculesOnlySearchResults,
                    onCheckboxChange(_.copy(selectedType = Some(Molecules), data = Empty,
                      moleculesOnlySearchResults = !s.moleculesOnlySearchResults))
                  ),
                  "Only search results"
                )
              )
            )
          ),

          //Scaffolds box
          <.a(CSS.listGroupItem, activeFor(Scaffolds), onClickSetET(Scaffolds),
            <.h4("Scaffolds"),
            "Export scaffolds on the current level of scaffold hierarchy. Not related to the loaded dataset."
          )

        ),

        <.div(CSS.textCenter,
          <.div(CSS.btnGroup, CSS.btnGroupLg, ^.marginTop := "1em",
            (s.data: @unchecked) match {
              case Empty => s.selectedType.nonEmpty ?=
                <.a(^.key := 1, CSS.btnPrimary, "Prepare data", ^.onClick --> $.modState(_.copy(data = s.data.pending())))
              case Pending(_) =>
                <.a(^.key := 2, ^.id := preparingDataButtonHtmlId,
                  CSS.btnPrimary, CSS.disabled, ^.disabled := true, "Preparing data, please wait...")
              case Ready(data) =>
                <.a(^.key := 3, CSS.btnPrimary, "Save Data", ^.href := dataUrl(data), ^.download := data.fileName)
              case Failed(e) =>
                <.a(^.key := 4, CSS.btnDanger, e.getMessage, CSS.disabled, ^.disabled := true)
            }
          )
        )
      )
    }

    def dataUrl(data: Data) = data match {
      case StringData(string, _) => DataUrl.blobUrl(string)
      case ByteArrayData(bytes, _) => DataUrl.blobUrl(bytes)
    }
  }

  def potFromTry[A](a: Try[A]): Pot[A] = a match {
    case Success(x) => Ready(x)
    case Failure(x) => Failed(x)
  }

  val preparingDataButtonHtmlId = "ExportFormPreparingDataBtn"

  def onUpdate($: DuringCallbackM[Props, State, Backend, TopNode]): Callback = {
    if($.state.data.isPending) { //pending = we should generate data
      val log: Callback = Callback.log("Generating data")
      val compute: CallbackTo[Try[Data]] = CallbackTo{
        Try($.state.selectedType.get)
          .flatMap(selected => prepareData(selected, $.state, $.props.currentScaffoldId, $.props.proxy.value))
      }
      val setState: (Try[Data]) => Callback = (data) => $.modState(_.copy(data = potFromTry(data)))

      //1) nice and not working - "Preparing..." does not draw
      //log >> compute >>= setState

      //2) hacky alternative to force redraw
      import DomHelpers._
      waitForElementByIdAndThenSome(preparingDataButtonHtmlId, waitAndExecute(compute >>= setState))
      log
    }
    else Callback.empty
  }

  def prepareData(selectedExportType: ExportType, state: State, currentScaffoldId: Option[Int], model: Model): Try[Data] = {
    selectedExportType match {

      case Scaffolds => Try {
        val parent = currentScaffoldId.getOrElse(fail("Could not get current position in hierarchy"))
        val scaffolds = model.scaffolds.scaffoldHierarchy.children(parent).getOrElse(fail("Could not get scaffolds"))
        if(scaffolds.isEmpty)
          fail("No scaffolds on this level")
        val fileExt = scaffolds.head match {
          case _: SmilesScaffold => "smiles"
          case _ => "txt"
        }
        val fileName = s"export-scaffolds.$fileExt"
        StringData(scaffolds.map(_.key).mkString("\n"), fileName)
      }

      case Molecules => Try {
        val molecules = model.molecules.getOrElse(fail("No dataset loaded"))

        var filterBySubtree = state.moleculesOnlySubtree
        var filterBySelected = state.moleculesOnlySelected
        val filterBySearchResults = state.moleculesOnlySearchResults

        val base: TraversableOnce[Molecule] =
          if(filterBySelected) {
            filterBySelected = false //not again
            molecules.selected.iterator.flatMap(molecules.get)
          } else if(filterBySubtree) {
            filterBySubtree = false
            molecules.scaffoldMolecules(currentScaffoldId.get).iterator.flatMap(molecules.get)
          } else {
            molecules.molecules
          }

        val subtree =
          if(filterBySubtree) {
            val scaffoldId: ScaffoldId = currentScaffoldId.get
            val scaffoldLvl = model.scaffolds.scaffoldHierarchy.apply(scaffoldId).level
            base.filter(m => m.scaffoldId(scaffoldLvl).contains(scaffoldId))
          } else {
            base
          }

        val selected =
          if(filterBySelected) {
            base.filter(m => molecules.selected.contains(m.id))
          } else {
            base
          }

        val searchResults =
          if(filterBySearchResults) {
            model.viewState.search match {
              case None => base
              case Some(s) => base.filter(m => m.smiles.contains(s) || m.name.exists(name => name.contains(s)))
            }
          } else {
            base
          }

        val result = searchResults
        if(result.isEmpty)
          fail("No matching molecules")
        StringData(result.map(_.smiles).mkString("\n"), fileName = "export-molecules.smiles")
      }

      case Dataset => Try {
        if(model.molecules.isEmpty)
          fail("No dataset loaded")
        ByteArrayData(BooPickleSerializer.save(model, state.datasetSelection), fileName = "dataset.scaffvis")
      }

      case _ => Failure(new NotImplementedError)
    }
  }

  def fail(errorMsg: String): Nothing = throw new RuntimeException(errorMsg)

  val component = ReactComponentB[Props]("ExportForm")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidUpdate(scope => onUpdate(scope.$))
    .build

  def apply(props: Props) = component(props)
}
