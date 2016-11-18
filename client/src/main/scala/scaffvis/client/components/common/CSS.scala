package scaffvis.client.components.common

import scalacss.Defaults._

/**
  * Provides safe access to CSS class names
  */
object CSS extends StyleSheet.Inline {

  import dsl._

  val listGroup = className("list-group")
  val listGroupItem = className("list-group-item")

  val close = className("close")

  val formGroup = className("form-group")
  val formControl = className("form-control")

  val btnDefault = className("btn btn-default")
  val btnPrimary = className("btn btn-primary")
  val btnDanger = className("btn btn-danger")
  val btnGroup = className("btn-group")
  val btnGroupLg = className("btn-group-lg")
  val btnGroupSm = className("btn-group-sm")
  val btnGroupXs = className("btn-group-xs")
  val btnGroupJustified = className("btn-group-justified")

  val active = className("active")
  val disabled = className("disabled")

  val inputGroup = className("input-group")

  val centerBlock = className("center-block")

  val textCenter = className("text-center")
  val textMuted = className("text-muted")

  val centeredBoxDefault = className("centered-box-default")

  val navbarForm = className("navbar-form")
  val navbarRight = className("navbar-right")
  val navbarBtn = className("navbar-btn")

  val modal = className("modal")
  val modalFade = className("fade")
  val modalDialog = className("modal-dialog")
  val modalContent = className("modal-content")
  val modalHeader = className("modal-header")
  val modalBody = className("modal-body")
  val modalFooter = className("modal-footer")

  val treemapMoleculeBg = className("treemap-molecule-bg")
  val treemapMoleculeFg = className("treemap-molecule-fg")
  val deselected = className("deselected")

  val listBoxScrollbox = className("list-box-scrollbox")
  val listBoxToolbar = className("list-box-toolbar")
  val pagination = className("pagination")

  val panel = className("panel panel-default")
  val panelHeading = className("panel-heading")
  val panelTitle = className("panel-title")
  val panelBody = className("panel-body")

  def className(classNames: String*) = style(addClassNames(classNames: _*))
}
