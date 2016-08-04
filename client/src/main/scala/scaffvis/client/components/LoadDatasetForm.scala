package scaffvis.client.components

import scaffvis.client.components.common.{CSS, GlyphIcon}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions.{LoadMoleculesFromJsFile, LoadMoleculesLocally}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, _}
import org.scalajs.dom._

import scalacss.ScalaCssReact._

object LoadDatasetForm {

  case class Props(submitHandler: () => Callback)

  case class State(file: Option[File] = None, cancelled: Boolean = true)

  class Backend($: BackendScope[Props, State]) {

    def submitForm(hide: Callback): Callback = {

      $.state >>= { state =>
        state.file match {
          case None =>
            Callback.log("No file selected") >> hide
          case Some(file) =>
            val loadAction =
              if (file.name.endsWith(".scaffvis")) LoadMoleculesLocally(file) //load locally
              else LoadMoleculesFromJsFile(file) //send to server
            Callback.log("Loading file") >> Store.dispatchCB(loadAction) >> hide
        }
      }
    }

    def formClosed(state: State, props: Props): Callback = props.submitHandler()

    def onChooseFile(e: ReactEventI) = {
      val fileList: FileList = e.currentTarget.files
      val file = if(fileList.length > 0) Some(fileList.apply(0)) else None
      $.modState(s => s.copy(file = file))
    }

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
          <.input.file(CSS.formControl, ^.id := "file",
            ^.onChange ==> onChooseFile
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
