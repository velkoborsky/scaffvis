package scaffvis.client.components.common

import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions.LoadMoleculeSvg
import scaffvis.client.store.actions.ScaffoldsActions.LoadScaffoldSvg
import scaffvis.client.store.model.{Molecules, Scaffolds}
import diode.Action
import diode.data.Pot
import japgolly.scalajs.react.Callback

import scala.collection.mutable

class SvgProvider(source: Map[Int, Pot[String]], loadMissingAction: Seq[Int] => Action) {

  val missingSvgs = mutable.Set.empty[Int]

  def getSvg(id: Int): Option[String] = {
    val svg = source.get(id).flatMap(_.toOption)
    if(svg.isEmpty)
      missingSvgs += id
    svg
  }

  private def loadSelectedMissingSvgCallback(selector: mutable.Set[Int] => TraversableOnce[Int]) = {
    if(missingSvgs.isEmpty)
      Callback.empty
    else {
      val ids = selector(missingSvgs).toVector
      missingSvgs.clear()
      Store.dispatchCB(loadMissingAction(ids))
    }
  }

  /**
    * Load all missing svgs.
    * @return
    */
  def loadMissingSvgCallback(): Callback = loadSelectedMissingSvgCallback(identity)

  /**
    * Load only some missing svgs, discard the others.
    * @param limit
    * @return
    */
  def loadMissingSvgCallback(limit: Int): Callback = loadSelectedMissingSvgCallback(_.iterator.take(limit))

}

object SvgProvider {

  def apply(source: Molecules) =
    new SvgProvider(source.svg, loadMissingAction = LoadMoleculeSvg.apply)

  def apply(source: Scaffolds) =
    new SvgProvider(source.svg, loadMissingAction = LoadScaffoldSvg.apply)

}
