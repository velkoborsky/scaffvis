package scaffvis.client.store

import scaffvis.client.store.handlers._
import scaffvis.client.store.model._
import diode.react.ReactConnector
import diode.{Action, Circuit}
import japgolly.scalajs.react.Callback

/**
  * Registers action handlers, dispatches actions and provides application state
  */
object Store extends Circuit[Model] with ReactConnector[Model] {

  val dispatchCB: (Action) => Callback = (action) => Callback(dispatch(action))

  override protected def initialModel = Model()

  override protected def actionHandler = composedHandlers

  val composedHandlers = {

    val viewStateHandler = new ViewStateHandler(
      zoomRW(model => model.viewState)((model, newValue) => model.copy(viewState = newValue))
    )

    val moleculesHandler = new MoleculesHandler(
      zoomRW(model => model.molecules)((model, newValue) => model.copy(molecules = newValue))
    )

    val scaffoldsHandler = new ScaffoldsHandler(
      zoomRW(model => model.scaffolds)((model, newValue) => model.copy(scaffolds = newValue))
    )

    composeHandlers(viewStateHandler, moleculesHandler, scaffoldsHandler)
  }

}
