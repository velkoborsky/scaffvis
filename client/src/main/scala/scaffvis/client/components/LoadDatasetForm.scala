package scaffvis.client.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, _}
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLInputElement
import scaffvis.client.components.common.{CSS, GlyphIcon}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions.{LoadMoleculesFromJsFile, LoadMoleculesFromSampleDataset, LoadMoleculesLocally}

import scalacss.ScalaCssReact._

object LoadDatasetForm {

  case class Props(submitHandler: () => Callback)

  sealed trait DatasetSelection
  case object NoDataset extends DatasetSelection
  case class UserDataset(file: File) extends DatasetSelection
  case class SampleDataset(name: String) extends DatasetSelection

  case class State(dataset: DatasetSelection = NoDataset, cancelled: Boolean = true)

  class Backend($: BackendScope[Props, State]) {

    def submitForm(hide: Callback): Callback = {

      $.state >>= { state =>
        state.dataset match {
          case NoDataset =>
            Callback.log("No dataset selected") >> hide
          case UserDataset(file) =>
            val loadAction =
              if (file.name.endsWith(".scaffvis")) LoadMoleculesLocally(file) //load locally
              else LoadMoleculesFromJsFile(file) //send to server
            Callback.log("Loading file") >> Store.dispatchCB(loadAction) >> hide
          case SampleDataset(name) =>
            Callback.log("Loading file") >> Store.dispatchCB(LoadMoleculesFromSampleDataset(name)) >> hide
        }
      }
    }

    def formClosed(state: State, props: Props): Callback = props.submitHandler()

    def onChooseFile(e: ReactEventI) = {
      val fileList: FileList = e.currentTarget.files
      val file = if(fileList.length > 0) UserDataset(fileList.apply(0)) else NoDataset
      $.modState(s => s.copy(dataset = file))
    }

    def onChooseSample(name: String) = {
      fileSelectorRef($).get.value = ""
      $.modState(s => s.copy(dataset = SampleDataset(name)))
    }

    val fileSelectorRef = Ref[HTMLInputElement]("fileSelectorRef")

    def render(p: Props, s: State) = {
      BootstrapModal(BootstrapModal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, CSS.close, ^.onClick --> hide, GlyphIcon.remove), <.h4("Load Dataset")),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, ^.onClick --> submitForm(hide), "Load")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.div(CSS.formGroup,
          <.label(^.`for` := "file", "Select a file to load"),
          <.input.file(CSS.formControl, ^.id := "file", ^.ref := fileSelectorRef,
            ^.onChange ==> onChooseFile
          )
        ),
        <.p("Please note that the dataset might take a long time to load and process. Expect up to one minute for " +
            "every ten thousand molecules in the dataset."
        ),
        <.p(CSS.textMuted, "In case you are not able to load your data set, it might help to load and save it using " +
          "OpenBabel (or a similar tool). The most reliable input formats are SMILES or SDF files, preferably gzipped."),

        <.p("In case you just want to explore Scaffvis and have no particular dataset in mind, you can try sample " +
          "datasets based on ",
          <.a(^.href := "http://www.drugbank.ca/", "DrugBank"),
          " or ",
          <.a(^.href := "https://www.ebi.ac.uk/chembl/sarfari/kinasesarfari", "Kinase SARfari"),
          ":"
        ),

        <.div(^.cls := "radio",
          <.label(
            <.input.radio(
              ^.onChange --> onChooseSample("drugbank"),
              ^.checked := (s.dataset == SampleDataset("drugbank"))
            ),
            "DrugBank"
          )),
        <.div(^.cls := "radio",
          <.label(
            <.input.radio(
              ^.onChange --> onChooseSample("kinasesarfari"),
              ^.checked := (s.dataset == SampleDataset("kinasesarfari"))
            ),
            "Kinase SARfari"
          )
        )
      )
    }

  }

  val component = ReactComponentB[Props]("LoadDatasetForm")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
