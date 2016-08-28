package scaffvis.client.components.common

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Wrapper for Bootstrap provided Glyphicons (http://glyphicons.com/)
 */
object GlyphIcon {
  type Icon = ReactElement

  def base(name: String): Icon = <.span(^.className := s"glyphicon glyphicon-$name")

  lazy val cog = base("cog")
  lazy val check = base("check")
  lazy val exclamationSign = base("exclamation-sign")
  lazy val openFile = base("open-file")
  lazy val questionSign = base("question-sign")
  lazy val refresh = base("refresh")
  lazy val remove = base("remove")
  lazy val saveFile = base("save-file")
  lazy val search = base("search")
  lazy val thLarge = base("th-large")
  lazy val thList = base("th-list")
  lazy val unchecked = base("unchecked")

  def checked_?(checked: Boolean) = if (checked) check else unchecked
}

