package scaffvis.client.store.handlers

import scaffvis.client.store.actions.ViewStateActions._
import scaffvis.client.store.model.ViewState
import diode.{ActionHandler, ModelRW}


class ViewStateHandler[M](model: ModelRW[M, ViewState]) extends ActionHandler(model) {

  override protected def handle = {
    case UpdateViewState(func) =>
      updated(func(value))
  }

}
