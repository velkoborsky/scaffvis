package scaffvis.client.components

import scaffvis.client.components.ScaffoldListBox.SortOrder
import scaffvis.client.components.ScaffoldTreeMap.{ColorSelectFunction, SizeSelectFunction, TransformationFunction}
import scaffvis.client.components.common.{CSS, GlyphIcon}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.ViewStateActions.UpdateViewState
import scaffvis.client.store.model.ViewState
import scaffvis.layout.Gradient
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, _}

import scalacss.ScalaCssReact._

object SettingsForm {

  case class Props(submitHandler: () => Callback, viewState: ViewState)

  case class State()

  class Backend($: BackendScope[Props, State]) {

    def formClosed(state: State, props: Props): Callback = props.submitHandler()

    def render(props: Props, s: State) = {
      import props._

      BootstrapModal(BootstrapModal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, CSS.close, ^.onClick --> hide, GlyphIcon.remove), <.h4("Settings")),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(<.button(^.`type` := "button", CSS.btnDefault, ^.onClick --> hide, "Close")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, props)),

        <.div(CSS.panel,
          <.div(CSS.panelHeading, <.div(CSS.panelTitle, <.h5("Scaffold List"))),
          <.div(CSS.panelBody,
            <.div(CSS.formGroup,
              <.label(^.`for` := "sortOrder", "Sort order"),
              <.select(CSS.formControl, ^.id := "sortOrder",
                ^.value := viewState.scaffoldListSortOrder.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = SortOrder.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(scaffoldListSortOrder = v)))
                    case None => Callback.log(s"Sort order $name does not exist")
                  }
                }),
                SortOrder.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            )
          )
        ),

        <.div(CSS.panel,
          <.div(CSS.panelHeading, <.div(CSS.panelTitle, <.h5("Scaffold Tree Map"))),
          <.div(CSS.panelBody,
            <.div(CSS.formGroup,
              <.label(^.`for` := "colorSelectF", "Color values source"),
              <.select(CSS.formControl, ^.id := "colorSelectF",
                ^.value := viewState.colorSelectFunction.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = ColorSelectFunction.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(colorSelectFunction = v)))
                    case None => Callback.log(s"Color select function $name does not exist")
                  }
                }),
                ColorSelectFunction.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            ),
            <.div(CSS.formGroup,
              <.label(^.`for` := "colorTransformationF", "Color values transformation"),
              <.select(CSS.formControl, ^.id := "colorTransformationF",
                ^.value := viewState.colorTransformationFunction.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = TransformationFunction.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(colorTransformationFunction = v)))
                    case None => Callback.log(s"Transformation function $name does not exist")
                  }
                }),
                TransformationFunction.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            ),
            <.div(CSS.formGroup,
              <.label(^.`for` := "colorGradientF", "Color gradient"),
              <.select(CSS.formControl, ^.id := "colorGradientF",
                ^.value := viewState.colorGradientFunction.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = Gradient.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(colorGradientFunction = v)))
                    case None => Callback.log(s"Gradient $name does not exist")
                  }
                }),
                Gradient.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            ),
            <.div(CSS.formGroup,
              <.label(^.`for` := "sizeSelectF", "Size source"),
              <.select(CSS.formControl, ^.id := "sizeSelectF",
                ^.value := viewState.sizeSelectFunction.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = SizeSelectFunction.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(sizeSelectFunction = v)))
                    case None => Callback.log(s"Size select function $name does not exist")
                  }
                }),
                SizeSelectFunction.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            ),
            <.div(CSS.formGroup,
              <.label(^.`for` := "sizeTransformationF", "Size transformation"),
              <.select(CSS.formControl, ^.id := "sizeTransformationF",
                ^.value := viewState.sizeTransformationFunction.name,
                ^.onChange ==> ((e: ReactEventI) => {
                  val name = e.currentTarget.value
                  val value = TransformationFunction.values.find(_.name == name)
                  value match {
                    case Some(v) => Store.dispatchCB(UpdateViewState(vs => vs.copy(sizeTransformationFunction = v)))
                    case None => Callback.log(s"Transformation function $name does not exist")
                  }
                }),
                TransformationFunction.values.map(_.name).map(n => <.option(^.value := n, n))
              )
            )
          )
        )

      )
    }

  }

  val component = ReactComponentB[Props]("SettingsForm")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}
