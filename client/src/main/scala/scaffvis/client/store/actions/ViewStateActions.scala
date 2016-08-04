package scaffvis.client.store.actions

import scaffvis.client.store.model.ViewState
import diode.Action

object ViewStateActions {

  case class UpdateViewState(func: ViewState => ViewState) extends Action

}