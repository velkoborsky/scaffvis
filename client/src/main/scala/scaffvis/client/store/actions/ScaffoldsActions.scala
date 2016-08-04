package scaffvis.client.store.actions

import scaffvis.shared.model.{Scaffold, _}
import diode.Action

object ScaffoldsActions {

  case class LoadChildren(scaffoldId: ScaffoldId) extends Action
  case class UpdateChildren(scaffoldId: ScaffoldId, children: Seq[Scaffold]) extends Action

  case class LoadScaffold(scaffoldId: ScaffoldId) extends Action
  case class StoreScaffold(chain: Seq[Scaffold]) extends Action

  case class LoadScaffoldSvg(scaffoldIds: Seq[ScaffoldId]) extends Action
  case class UpdateScaffoldSvg(scaffoldIds: Seq[ScaffoldId], svg: Seq[String]) extends Action
}
