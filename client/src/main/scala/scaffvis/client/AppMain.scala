package scaffvis.client

import scaffvis.client.components.common.CSS
import scaffvis.client.components.{Header, MainComponent}
import scaffvis.client.store.Store
import scaffvis.shared.model.{RootScaffold, ScaffoldId}
import japgolly.scalajs.react.{ReactDOM, ReactElement}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.annotation.elidable
import scala.annotation.elidable._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

@JSExport("AppMain")
object AppMain extends js.JSApp {

  //define the locations (pages) used in this application
  sealed trait Loc

  case class ScaffoldLoc(scaffoldId: ScaffoldId = RootScaffold.id) extends Loc

  //create a react component that surrounds other components and provides ModelProxy
  val ModelWrapper = Store.connect(model => model)

  //configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    ( dynamicRouteCT("#scaffold" / int.caseClass[ScaffoldLoc])  ~>
        dynRenderR[ScaffoldLoc, ReactElement]((page, routerCtl) =>
          ModelWrapper(proxy => MainComponent(proxy = proxy, router = routerCtl, page = page))
        )
      ).notFound(redirectToPage(ScaffoldLoc())(Redirect.Replace))

  }.renderWith(layout)

  //base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    <.div(
      ModelWrapper(proxy => Header(c, r.page, proxy)),
      // currently active module is shown
      <.div(r.render())
    )
  }

  @JSExport
  def main(): Unit = {
    //integrate ScalaCSS
    CSS.addToDocument()
    //create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    //provide access to React Perf tools from browser
    bindReactPerfToGlobal()
    //use React to render the router in the specified container
    ReactDOM.render(router(), dom.document.getElementById("react-container"))
  }

  @elidable(INFO) //in production this code will disapper with no performance penalty
  def bindReactPerfToGlobal(): Unit = {
    js.Dynamic.global.Perf = japgolly.scalajs.react.Addons.Perf
  }

}
