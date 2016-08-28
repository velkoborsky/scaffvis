package scaffvis.client.components

import scaffvis.client.AppMain.{ScaffoldLoc, Loc}
import scaffvis.client.components.common.{CSS, Svg, SvgProvider}
import scaffvis.client.store.Store
import scaffvis.client.store.actions.ScaffoldsActions.LoadChildren
import scaffvis.client.store.model.{Model, ScaffoldHierarchy}
import scaffvis.layout.Rect
import scaffvis.shared.model._
import diode.data.{Empty, Failed, Pending, Ready}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scalacss.ScalaCssReact._

/**
  * The left main box - scaffold tree map, scaffold list or molecule gallery
  */
object LeftBox {

  case class Props(model: Model, router: RouterCtl[Loc], currentScaffold: Scaffold, tooltipControl: TooltipControl)

  def apply(props: Props) = component(props)

  val canvas = Rect(x = 0, y = 0, w = 1000, h = 1000)
  def canvasCommonTagMods(id: String) = Seq(
    ^.id := id, ^.className := "leftbox",
    ^.svg.viewBox := Svg.viewBoxFromRect(canvas, padding = 5),
    ^.svg.preserveAspectRatio := "xMidYMid"
  )

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {

      import props._
      import model._

      val scaffoldTree: ScaffoldHierarchy = scaffolds.scaffoldHierarchy

      lazy val zoomOut: Callback =
        scaffoldTree.parent(currentScaffold) match {
          case Some(parent) => router.set(ScaffoldLoc(parent.id))
          case None => Callback.empty
        }

      lazy val zoomIn: ScaffoldId => Callback =
        scaffoldId => router.set(ScaffoldLoc(scaffoldId))

      val onWheelDownZoomOut: ReactWheelEvent => Callback = e => {
        e.preventDefault()
        if (e.deltaY < 0) //scroll up ~ zoom
          Callback.empty
        else
          zoomOut
      }

      <.div(^.id := "LeftBox", ^.className := "leftbox",
        (! model.viewState.showScaffoldsAsList) ?= ^.onWheel ==> onWheelDownZoomOut,
        if (currentScaffold.level < HierarchyLevels.bottomLvl) {
          //scaffolds
          scaffoldTree.children(currentScaffold) match {
            case Ready(children) => {
              ScaffoldBox(
                ScaffoldBox.Props(currentScaffold, children,
                  svgProvider = SvgProvider(scaffolds), //create a new one so that we can use limited requests in ScaffoldTreeMap
                  molecules = molecules,
                  tooltipControl = tooltipControl,
                  zoomOut = zoomOut,
                  zoomIn = zoomIn,
                  onWheelDownZoomOut = onWheelDownZoomOut,
                  viewState = model.viewState
                )
              )
            }
            case _ => {
              <.div(CSS.centeredBoxDefault, "Loading scaffolds")
            }
          }
        } else {
          //molecule gallery
          molecules match {
            case Ready(ms) =>
              MoleculeGallery(
                MoleculeGallery.Props(currentScaffold, ms,
                  tooltipControl = tooltipControl,
                  onWheelDownZoomOut = onWheelDownZoomOut
                )
              )
            case Pending(_) =>
              <.div(CSS.centeredBoxDefault, "Loading molecules")
            case Empty =>
              <.div(CSS.centeredBoxDefault, "No dataset loaded")
            case Failed(_) =>
              <.div(CSS.centeredBoxDefault, "Loading dataset failed")
            case _ =>
              <.div(CSS.centeredBoxDefault, "Unexpected state")
          }
        }
      )
    }
  }

  def loadChildrenIfMissing(props: Props): Callback =
    loadChildrenIfMissing(props.currentScaffold.id, props.model.scaffolds.scaffoldHierarchy)

  def loadChildrenIfMissing(currentScaffoldId: ScaffoldId, scaffoldTree: ScaffoldHierarchy): Callback = {
    val children = scaffoldTree.children(currentScaffoldId)
    Callback.when(children.isEmpty)(Store.dispatchCB(LoadChildren(currentScaffoldId)))
  }

  def hideTooltipOnScaffoldChange(currentProps: Props, nextProps: Props): Callback = {
    val prevId = currentProps.currentScaffold.id
    val nextId = nextProps.currentScaffold.id
    if(nextId != prevId) currentProps.tooltipControl.hideTooltip
    else Callback.empty
  }

  val component = ReactComponentB[Props]("LeftBox")
    .renderBackend[Backend]
    .componentDidMount(scope => loadChildrenIfMissing(scope.props))
    .componentDidUpdate(scope => loadChildrenIfMissing(scope.$.props))
    .componentWillUpdate(scope => hideTooltipOnScaffoldChange(scope.currentProps, scope.nextProps))
    .build
}



