package scaffvis.client.store.model

import diode.data.{Empty, Pot}

/**
  * Represents (almost all) client application state
  *
  * @param molecules currently loaded dataset
  */
case class Model(
                  scaffolds: Scaffolds = Scaffolds(),
                  molecules: Pot[Molecules] = Empty,
                  viewState: ViewState = ViewState()
                )
