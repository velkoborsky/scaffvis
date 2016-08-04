package scaffvis.client.components

import scaffvis.ReusableComputation
import scaffvis.client.components.common.{CSS, SvgProvider}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLDivElement

import scala.collection.SortedSet
import scalacss.ScalaCssReact._

object ListBox {


  case class Props[T](htmlId: String, items: Either[String, Items[T]], tooltipControl: TooltipControl)

  case class State(page: Int = 1, maximumSeenPage: Int = 1)

  case class Items[T](items: Iterable[T],
                      id: T => Int,
                      heading: T => String,
                      lines: T => Seq[String],
                      isSelected: T => Boolean,
                      isActive: T => Boolean,
                      isEnabled: T => Boolean = (_: T) => true,
                      isSelectable: T => Boolean = (_: T) => true,
                      svgProvider: SvgProvider,
                      onClick: (Boolean, Int) => ReactMouseEvent => Callback,
                      onMouseEnter: (Int) => Callback,
                      onMouseLeave: (Int) => Callback,
                      itemButton: Option[ListBoxItem.Button] = None,
                      toolbarButtons: Seq[Button] = Nil
                     )

  case class Button(content: String, onClick: Callback, enabled: Boolean = true)

  val pageSize: Int = 50

  case class Pager[T](stream: Stream[Seq[T]]){
    def page(pageNumber: Int): Seq[T] = stream(pageNumber - 1)
    def pageExists(pageNumber: Int): Boolean = stream.isDefinedAt(pageNumber - 1)
  }

  case class PageLink(text: String, toPage: Option[Int], isActive: Boolean = false) {
    def isDisabled = toPage.isEmpty
  }

  object Pagination {

    def links(pageNumber: Int, maximumSeenPage: Int, pager: Pager[_]): Seq[PageLink] = {
      val hasPrev = pageNumber > 1
      val prev = PageLink("«", toPage = if(hasPrev) Some(pageNumber - 1) else None)

      val hasNext: Boolean = pager.pageExists(pageNumber + 1)
      val next = PageLink("»", toPage = if(hasNext) Some(pageNumber + 1) else None)

      val max: Int = if(hasNext) Math.max(maximumSeenPage, pageNumber + 1) else maximumSeenPage

      def clippedRange(from: Int, to: Int) = Range.inclusive(Math.max(from, 1), Math.min(to, max)).to[SortedSet]

      val beginning = clippedRange(1, 3)
      val mid = clippedRange(pageNumber - 2, pageNumber + 2)
      val end = clippedRange(maximumSeenPage - 2, max)
      val numbers = beginning union mid union end

      val all = Seq(
        Seq(prev),
        numbers.toSeq.map(n => PageLink(n.toString, toPage = Some(n), isActive = n == pageNumber)),
        Seq(next)
      )
      all.flatten
    }

  }

  class Backend[T]($: BackendScope[Props[T], State]) {

    def render(props: Props[T], state: State) = {

      <.div(^.id := props.htmlId,
        props.items match {
          case Left(msg) => <.div(CSS.centeredBoxDefault, msg)
          case Right(items) =>

            val pager = getPagerCached(items.items)
            Seq(

              //bottom toolbar
              <.div(CSS.listBoxToolbar,
                <.ul(CSS.pagination,
                  Pagination.links(state.page, state.maximumSeenPage, pager).map(renderPageLink)
                ),
                <.div(CSS.btnGroup,
                  items.toolbarButtons.map(b =>
                    <.button(CSS.btnDefault, ^.onClick --> b.onClick, b.content,
                      (!b.enabled) ?= Seq[TagMod](^.disabled := true, CSS.disabled)
                    )
                  )
                )
              ),

              //main content
              <.div(CSS.listBoxScrollbox, ^.ref := mainContentScrollboxRef,
                <.div(CSS.listGroup,
                  pager.page(state.page).map(item => {
                    val id = items.id(item)
                    ListBoxItem(ListBoxItem.Props(
                      id = id, heading = items.heading(item), lines = items.lines(item),
                      svg = items.svgProvider.getSvg(id),
                      isEnabled = items.isEnabled(item), isSelected = items.isSelected(item),
                      isActive = items.isActive(item), isSelectable = items.isSelectable(item),
                      onClick = items.onClick, onMouseEnter = items.onMouseEnter, onMouseLeave = items.onMouseLeave,
                      button = items.itemButton, tooltipControl = props.tooltipControl
                    ))
                  }).toReactNodeArray
                )
              )

            )
        }
      )
    }

    def renderPageLink(l: PageLink): ReactElement = {
      import l._
      <.li(
        isActive ?= CSS.active,
        isDisabled ?= CSS.disabled,
        <.a(
          l.text,
          ^.href := "#",
          ^.onClick ==> ((e: SyntheticEvent[_]) => {
            e.preventDefault
            toPage match {
              case Some(p) => goToPage(p)
              case None => Callback.empty
            }
          })
        )
      )
    }

    def goToPage(p: Int): Callback = $.modState(s => s.copy(
      page = p,
      maximumSeenPage = Math.max(p, s.maximumSeenPage)
    ))

    def getPager(items: Iterable[T]): Pager[T] = Pager(items.iterator.grouped(pageSize).toStream)
    val getPagerCached = ReusableComputation.simpleE(getPager _, ReusableComputation.anyRefEq)

    val mainContentScrollboxRef = Ref[HTMLDivElement]("mainContentScrollbox")
    def resetScrollPosition() = mainContentScrollboxRef($).foreach(_.scrollTop = 0)
  }

  /**
    * resets pagination if needed
    */
  def onComponentWillReceivePropsResetPagination[T](scope: ComponentWillReceiveProps[Props[T], State, Backend[T], TopNode]): Callback = {
    import scope.$
    val p = scope.nextProps
    p.items match {
      case Left(_) => $.setState(State())
      case Right(items) =>
        val pager = $.backend.getPagerCached(items.items)
        val s = scope.currentState
        if (pager.pageExists(s.maximumSeenPage))
          Callback.empty
        else
          $.setState(State())
    }
  }

  /**
    * loads missing SVGs
    */
  def onComponentRenderedLoadMissingSVG[T](props: Props[T]): Callback =
    props.items match {
      case Right(items) => items.svgProvider.loadMissingSvgCallback()
      case _ => Callback.empty
    }

  def onPageChangeResetScrollPosition[T](scope: ComponentDidUpdate[Props[T], State, Backend[T], TopNode]) = Callback {
    val prevPage = scope.prevState.page
    val currentPage = scope.currentState.page
    if (prevPage != currentPage)
      scope.$.backend.resetScrollPosition()
  }

  def component[T] = ReactComponentB[Props[T]]("ListBox")
    .initialState(State())
    .renderBackend[Backend[T]]
    .componentWillReceiveProps(onComponentWillReceivePropsResetPagination)
    .componentDidMount(scope => onComponentRenderedLoadMissingSVG(scope.props))
    .componentDidUpdate(scope =>
      onPageChangeResetScrollPosition(scope) >>
      onComponentRenderedLoadMissingSVG(scope.currentProps))
    .build

}
