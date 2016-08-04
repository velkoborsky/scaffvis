package scaffvis.client.store.handlers

import autowire._
import scaffvis.client.services._
import scaffvis.client.store.actions.ScaffoldsActions._
import scaffvis.client.store.handlers.common.SvgHandlerHelper
import scaffvis.client.store.model.{ScaffoldHierarchy, Scaffolds}
import scaffvis.shared.Api
import diode.data.{Pending, Ready}
import diode.{ActionHandler, Effect, ModelRW}

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Reads scaffolds hierarchy data from server
  */
class ScaffoldsHandler[M](model: ModelRW[M, Scaffolds]) extends ActionHandler(model) {

  override def handle = {
    case LoadChildren(scaffoldId) =>
      val currentChildren = value.scaffoldHierarchy.children(scaffoldId)
      val shouldGet = currentChildren match {
        case Ready(_) => false
        case p: Pending => p.duration() > loadChildrenRetryInterval
        case _ => true
      }
      if(shouldGet)
        updated(
          newValue = updatedHierarchy(_.storeChildren(scaffoldId, Pending())),
          effect = Effect(AutowireClient[Api].getChildren(scaffoldId).call().map(chs => UpdateChildren(scaffoldId, chs)))
        )
      else noChange
    case LoadScaffold(scaffoldId) =>
      //println(s"Requesting scaffold $scaffoldId")
      effectOnly(Effect(AutowireClient[Api].getScaffold(scaffoldId).call().map(chain => StoreScaffold(chain))))
    case UpdateChildren(scaffoldId, children) =>
      //println(s"Updating children for $scaffoldId, count: ${children.size}")
      updated(
        newValue = updatedHierarchy(_.storeChildren(scaffoldId, Ready(children)))
      )
    case StoreScaffold(chain) =>
      //println(s"Storing chain: ${chain.map(_.id).mkString(",")}")
      updated(updatedHierarchy(_.storeScaffold(chain)))


    case LoadScaffoldSvg(scaffoldIds) =>
      val currentMap = value.svg
      val idsToLoad = SvgHandlerHelper.getIdsToLoad(currentMap, scaffoldIds)
      if(idsToLoad.isEmpty) noChange
      else updated(
        newValue = value.copy(svg = SvgHandlerHelper.updateWithPending(currentMap, idsToLoad)),
        effect = Effect(AutowireClient[Api].getScaffoldSvg(idsToLoad).call().map(svg => UpdateScaffoldSvg(idsToLoad, svg))
        )
      )

    case UpdateScaffoldSvg(scaffoldIds, svgs) =>
      updated(value.copy(svg = SvgHandlerHelper.updateWithNewSvgs(value.svg, scaffoldIds, svgs)))
  }

  def updatedHierarchy(f: ScaffoldHierarchy => ScaffoldHierarchy): Scaffolds = {
    val currentHierarchy = value.scaffoldHierarchy
    val newHierarchy = f(currentHierarchy)
    value.copy(scaffoldHierarchy = newHierarchy)
  }

  val loadChildrenRetryInterval: Int = 1.minute.toMillis.toInt
}
