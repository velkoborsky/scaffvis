package scaffvis.shared.model

case class Molecule(
                     id: MoleculeId,
                     smiles: String,
                     name: Option[String],
                     comment: Option[String],
                     scaffolds: Array[ScaffoldId] //level -> scaffoldId
                   ) {
  def scaffoldId(level: Int) =
    if (scaffolds.length > level) Some(scaffolds(level))
    else None

  def isInSubtree(scaffold: Scaffold) = scaffoldId(scaffold.level).contains(scaffold.id)
}
