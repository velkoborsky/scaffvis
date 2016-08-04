package scaffvis.client.components

import scaffvis.client.AppMain.{ScaffoldLoc, Loc}
import scaffvis.client.components.common.{CSS, GlyphIcon}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.ViewStateActions.UpdateViewState
import scaffvis.client.store.model.Model
import scaffvis.shared.model.ScaffoldId
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.{HTMLFormElement, HTMLInputElement}

import scalacss.ScalaCssReact._

object Header {

  case class Props(routerCtl: RouterCtl[Loc], loc: Loc, proxy: ModelProxy[Model])

  def apply(routerCtl: RouterCtl[Loc], loc: Loc, proxy: ModelProxy[Model]) =
    component(Props(routerCtl, loc, proxy))

  case class State(
                    showLoadDatasetForm: Boolean = false,
                    showExportForm: Boolean = false,
                    showSettingsForm: Boolean = false
                  )

  class Backend($: BackendScope[Props, State]) {

    def render(props: Props, state: State) = {

      val viewState = props.proxy.value.viewState

      val selectedBtnClass = Seq(CSS.btnPrimary, CSS.active)
      val defaultBtnClass = Seq(CSS.btnDefault)
      def navbarBtnClassMod(selected: Boolean) = if (selected) selectedBtnClass else defaultBtnClass
      def navbarBtnClassAndIconMod(selected: Boolean) = navbarBtnClassMod(selected) + GlyphIcon.checked_?(selected)

      val currentScaffoldId: Option[ScaffoldId] = props.loc match {
        case ScaffoldLoc(scaffoldId) => Some(scaffoldId)
        case _ => None
      }

      <.div(^.id := "Header",
        <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",

          <.a(^.className := "navbar-brand", ^.href := "#", "Scaffold Visualizer"),

          //open, export buttons
          <.div(CSS.btnGroup,^.marginLeft := 15.px,
            <.button(^.`type` := "button", CSS.btnDefault, CSS.navbarBtn,
              GlyphIcon.openFile, " Open",
              ^.onClick --> $.modState(_.copy(showLoadDatasetForm = true))
            ),
            <.button(^.`type` := "button", CSS.btnDefault, CSS.navbarBtn,
              GlyphIcon.saveFile, " Export",
              ^.onClick --> $.modState(_.copy(showExportForm = true))
            )
          ),

          //scaffold treemap/list, settings buttons
          <.div(CSS.btnGroup, ^.marginLeft := 15.px,
            <.button(^.`type` := "button", CSS.navbarBtn,
              navbarBtnClassMod(!viewState.showScaffoldsAsList),
              GlyphIcon.thLarge, " Scaffold Tree Map",
              ^.onClick --> Store.dispatchCB(UpdateViewState(f => f.copy(showScaffoldsAsList = false)))
            ),
            <.button(^.`type` := "button", CSS.navbarBtn,
              navbarBtnClassMod(viewState.showScaffoldsAsList),
              GlyphIcon.thList, " Scaffold List",
              ^.onClick --> Store.dispatchCB(UpdateViewState(f => f.copy(showScaffoldsAsList = true)))
            ),
            <.button(^.`type` := "button", CSS.btnDefault, CSS.navbarBtn,
              GlyphIcon.cog, " Settings",
              ^.onClick --> $.modState(_.copy(showSettingsForm = true))
            )
          ),

          //filter box
          <.form(CSS.navbarForm, CSS.navbarRight,
            ^.marginRight := 0.px,
            ^.onSubmit ==> filterOnSubmit,
            ^.ref := filterFormRef,

            <.div(^.className := "input-group",
              <.input.text(^.className := "form-control",
                ^.placeholder := viewState.search.getOrElse("Filter"),
                ^.ref := filterInputRef),
              <.span(^.className := "input-group-btn",
                <.button(^.`type` := "submit", ^.className := "btn btn-default", GlyphIcon.search)
              )
            )
          ),

          //show only subtree/selected/search results
          <.div(CSS.navbarForm, CSS.navbarRight, ^.paddingRight := 0.px,
            <.div(CSS.btnGroup,
              <.button(^.key := "show_only_subtree",
                navbarBtnClassAndIconMod(selected = viewState.showOnlySubtree),
                ^.onClick --> Store.dispatchCB(UpdateViewState(f => f.copy(showOnlySubtree = !viewState.showOnlySubtree))),
                " Show only subtree"),
              <.button(^.key := "show_only_selected",
                navbarBtnClassAndIconMod(selected = viewState.showOnlySelected),
                ^.onClick --> Store.dispatchCB(UpdateViewState(f => f.copy(showOnlySelected = !viewState.showOnlySelected))),
                " Show only selected"),
              <.button(^.key := "show_only_search_results",
                navbarBtnClassAndIconMod(selected = viewState.showOnlySearchResults),
                ^.onClick --> Store.dispatchCB(UpdateViewState(f => f.copy(showOnlySearchResults = !viewState.showOnlySearchResults))),
                " Show only search results")
            )
          )

        ),

        //open, export, settings forms
        state.showLoadDatasetForm ?= LoadDatasetForm(
          LoadDatasetForm.Props(submitHandler = loadDatasetFormOnSubmit)
        ),
        state.showExportForm ?= ExportForm(
          ExportForm.Props(submitHandler = exportFormOnSubmit, proxy = props.proxy, currentScaffoldId = currentScaffoldId )
        ),
        state.showSettingsForm ?= SettingsForm(
          SettingsForm.Props(submitHandler = settingsFormOnSubmit, viewState = viewState)
        )
      )
      
    }

    def loadDatasetFormOnSubmit() = $.modState(s => s.copy(showLoadDatasetForm = false)) // hide the modal
    def exportFormOnSubmit() = $.modState(s => s.copy(showExportForm = false))
    def settingsFormOnSubmit() = $.modState(s => s.copy(showSettingsForm = false))

    val filterOnSubmit: ReactEvent => Callback = {e => $.props.flatMap(props => {
      e.preventDefault()
      val rawSearchText = filterInputRef($).get.value
      filterFormRef($).map(_.reset())
      val search = rawSearchText.trim match {
        case s if s.isEmpty => None
        case s => Some(s)
      }
      Store.dispatchCB(UpdateViewState(filter => filter.copy(search = search)))
    })}

    val filterFormRef = Ref[HTMLFormElement]("filterFormElement")
    val filterInputRef = Ref[HTMLInputElement]("filterInputElement")

  }

  val component = ReactComponentB[Props]("Header")
    .initialState(State())
    .renderBackend[Backend]
    .build

}
