package scaffvis.client.components

import scaffvis.client.components.common.CSS
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery}

import scala.language.implicitConversions
import scala.scalajs.js
import scalacss.ScalaCssReact._

object BootstrapModal {

  // header and footer are functions, so that they can get access to the the hide() function for their buttons
  case class Props(header: Callback => ReactNode, footer: Callback => ReactNode, closed: Callback, backdrop: Boolean = true,
                   keyboard: Boolean = true)

  class Backend(t: BackendScope[Props, Unit]) {
    def hide = Callback {
      // instruct Bootstrap to hide the modal
      jQuery(t.getDOMNode()).modal("hide")
    }

    // jQuery event handler to be fired when the modal has been hidden
    def hidden(e: JQueryEventObject): js.Any = {
      // inform the owner of the component that the modal was closed/hidden
      t.props.flatMap(_.closed).runNow()
    }

    def render(p: Props, children: PropsChildren) = {
      <.div(CSS.modal, CSS.modalFade,
        <.div(CSS.modalDialog,
          <.div(CSS.modalContent,
            <.div(CSS.modalHeader, p.header(hide)),
            <.div(CSS.modalBody, children),
            <.div(CSS.modalFooter, p.footer(hide))
          )
        )
      )
    }
  }

  val component = ReactComponentB[Props]("BootstrapModal")
    .renderBackend[Backend]
    .componentDidMount(scope => Callback {
      val p = scope.props
      // instruct Bootstrap to show the modal
      jQuery(scope.getDOMNode()).modal(js.Dynamic.literal("backdrop" -> p.backdrop, "keyboard" -> p.keyboard, "show" -> true))
      // register event listener to be notified when the modal is closed
      jQuery(scope.getDOMNode()).on("hidden.bs.modal", null, null, scope.backend.hidden _)
    })
    .build

  @js.native
  trait BootstrapJQuery extends JQuery {
    def modal(action: String): BootstrapJQuery = js.native
    def modal(options: js.Any): BootstrapJQuery = js.native
  }

  implicit def jQueryToBootstrap(jQuery: JQuery): BootstrapJQuery = jQuery.asInstanceOf[BootstrapJQuery]

  def apply(props: Props, children: ReactElement*) = component(props, children: _*)
}
