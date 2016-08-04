package scaffvis.client.components

import scaffvis.client.AppMain.{ScaffoldLoc, Loc}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.ScaffoldsActions.LoadScaffold
import scaffvis.client.store.model.Model
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js.annotation.JSExport

@JSExport("MainComponent")
object MainComponent {

  case class Props(proxy: ModelProxy[Model], router: RouterCtl[Loc], page: ScaffoldLoc)

  def apply(proxy: ModelProxy[Model], router: RouterCtl[Loc], page: ScaffoldLoc) =
    component(Props(proxy = proxy, router = router, page = page))

  //for performance testing - allows to run the main two components in separation
  var showBreadcrumb: Boolean = true
  var showLeftBox: Boolean = true
  var showMoleculeListBox: Boolean = true
  @JSExport def toggleBreadcrumb(): Unit = {showBreadcrumb = ! showBreadcrumb}
  @JSExport def toggleLeftBox(): Unit = {showLeftBox = ! showLeftBox}
  @JSExport def toggleMoleculeListBox(): Unit = {showMoleculeListBox = ! showMoleculeListBox}

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props) = {
      import props._
      val model = proxy.value
      val currentScaffoldId = page.scaffoldId
      val scaffoldTree = model.scaffolds.scaffoldHierarchy

      scaffoldTree.get(currentScaffoldId) match {
        case Some(currentScaffold) =>
          <.div(
            showBreadcrumb ?= (^.className := "with-breadcrumb"),
            model.viewState.showScaffoldsAsList ?= (^.className := "scaffolds-as-list"),
            showBreadcrumb ?= Breadcrumb(Breadcrumb.Props(
              proxy.zoom(model => model.scaffolds), router, currentScaffoldId, tooltipControl
            )),
            showLeftBox ?= LeftBox(LeftBox.Props(model, router, currentScaffold, tooltipControl)),
            showMoleculeListBox ?= MoleculeListBox(MoleculeListBox.Props(model, currentScaffold, tooltipControl)),
            Tooltip(tooltipRef)(Tooltip.Props(model.scaffolds, model.molecules)),
            Footer(Footer.Props(model, currentScaffold))
          )
        case None =>
          <.div("Loading...")
      }
    }

    val tooltipRef: RefComp[Tooltip.Props, Tooltip.State, Tooltip.Backend, TopNode] = Ref.to(Tooltip.component, "tooltip")
    val tooltipControl = TooltipControl(() => tooltipRef($))

  }

  val component = ReactComponentB[Props]("MainComponent")
    .renderBackend[Backend]
    .componentWillMount(ctx => {
      val Props(proxy, _, page) = ctx.props
      val currentScaffoldId = page.scaffoldId
      val scaffoldTree = proxy.value.scaffolds.scaffoldHierarchy
      Callback.when(scaffoldTree.get(currentScaffoldId).isEmpty)(Store.dispatchCB(LoadScaffold(currentScaffoldId)))
    })
    .build

}