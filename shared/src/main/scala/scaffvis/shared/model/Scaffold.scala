package scaffvis.shared.model

sealed trait Scaffold {
  def id: ScaffoldId

  def level: Int

  def key: String

  def subtreeSize: Int

  def shortName: String
}

case object RootScaffold extends Scaffold {

  override val id: ScaffoldId = 1
  override val level = 0

  private var _subtreeSize: Option[Int] = None
  override def subtreeSize: Int =
    _subtreeSize.getOrElse(throw new UnsupportedOperationException("Subtree size not available for the root element"))
  def setSubtreeSize(n: Int) = if (_subtreeSize.isEmpty) _subtreeSize = Some(n)

  override def key = "ROOT"
  override def shortName = "L0:ROOT"
}

case class RingCountScaffold(id: ScaffoldId,
                             ringCount: Int,
                             level: Int,
                             subtreeSize: Int,
                             svg: Option[String] = None
                            ) extends Scaffold {
  override def key = ringCount.toString
  override def shortName = s"L$level:RC$ringCount"
}

case class SmilesScaffold(id: ScaffoldId,
                          smiles: String,
                          level: Int,
                          subtreeSize: Int,
                          svg: Option[String] = None
                         ) extends Scaffold {
  override def key = smiles.toString
  override def shortName = s"L$level:S$smiles"
}