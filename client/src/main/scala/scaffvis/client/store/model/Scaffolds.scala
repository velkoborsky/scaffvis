package scaffvis.client.store.model

import scaffvis.client.components.common.SvgProvider
import scaffvis.shared.model._
import diode.data.Pot


case class Scaffolds(
                      scaffoldHierarchy: ScaffoldHierarchy = ScaffoldHierarchy.empty,
                      svg: Map[ScaffoldId, Pot[String]] = Map.empty
                    ) {
  lazy val svgProvider = SvgProvider(this)
}
