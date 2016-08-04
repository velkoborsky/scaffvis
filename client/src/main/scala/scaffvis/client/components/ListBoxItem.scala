package scaffvis.client.components

import scaffvis.client.components.common.{CSS, Svg}
import scaffvis.layout.Rect
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scalacss.ScalaCssReact._

object ListBoxItem {

  case class Props(id: Int, svg: Option[String], heading: String, lines: Seq[String],
                   isEnabled: Boolean, isSelected: Boolean, isActive: Boolean, isSelectable: Boolean = true,
                   onClick: (Boolean, Int) => ReactMouseEvent => Callback,
                   onMouseEnter: (Int) => Callback,
                   onMouseLeave: (Int) => Callback,
                   button: Option[Button] = None,
                   tooltipControl: TooltipControl
                  )

  case class Button(content: String, onClick: (Int) => Callback)

  def apply(props: Props) = component.withKey(props.id)(props)

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      import props._

      <.a(
        ^.className := {
          val sb = new StringBuilder(48, "list-group-item list-box-item")
          if (isActive) sb.append(" active")
          if (!isEnabled) sb.append(" disabled")
          sb.toString()
        },

        ^.onClick ==> onClick(isSelected, id),
        ^.onMouseEnter --> onMouseEnter(id),
        ^.onMouseLeave --> onMouseLeave(id),

        <.div(^.display := "table", ^.width := "100%",
          <.div(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px", ^.width := "1%",
            Svg.htmlImgFromSvgContent(svg.getOrElse(""), Svg.moleculeSvgViewBox, ^.width := 6.em,
              ^.onMouseOver ==> tooltipControl.showTooltipImageOpt(svg),
              ^.onMouseMove ==> tooltipControl.moveTooltip,
              ^.onMouseOut --> tooltipControl.hideTooltip
            )
          ),
          <.div(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px 6px 0px", ^.maxWidth := 0.px,
            <.span(^.className := "list-group-item-heading",
              heading
            ),
            lines.map(text => <.p(^.className := "list-group-item-text", text))
          ),
          button.map(b =>
            <.span(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px", ^.width := "1%",
              <.button(
                CSS.btnDefault, b.content,
                ^.onClick ==> { (e: ReactMouseEvent) => e.stopPropagation(); b.onClick(id) }
              )
            )
          ).getOrElse(EmptyTag),
          <.span(^.display := "table-cell", ^.verticalAlign := "middle", ^.padding := "6px 12px", ^.width := "1%",
            <.input.checkbox(^.checked := isSelected, ^.readOnly := true, (!isSelectable) ?= (^.disabled := true))
          )
        )
      )
    }
  }


  val component = ReactComponentB[Props]("ListBoxItem")
    .renderBackend[Backend]
    .shouldComponentUpdate(scope => {
      val isTheSame = scope.nextProps == scope.currentProps
      val shouldUpdate = !isTheSame
      shouldUpdate
    })
    .build

}
