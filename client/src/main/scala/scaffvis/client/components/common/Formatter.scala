package scaffvis.client.components.common

import scaffvis.shared.model._

import scala.util.Try

/**
  * Common formatter for use in ListBoxes, Tooltip, etc.
  */
object Formatter {

  val scaffoldHeading: Scaffold => String = {
    case s: SmilesScaffold => s"SMILES: ${s.smiles}"
    case s: RingCountScaffold => s"Ring count: ${s.ringCount}"
    case RootScaffold => s"Hierarchy root"
    case s => s.key
  }

  val scaffoldLines: Option[Scaffold => Int] => Scaffold => Seq[String] =
    scaffoldMoleculesCount => s => Seq(
      Seq(s"Level: ${s.level} (${HierarchyLevels.name(s.level)})"),
      scaffoldMoleculesCount.map(f => s"Dataset molecules: ${f(s)}").toSeq,
      Try(s"Pubchem compounds: ${s.subtreeSize}").toOption.toSeq
    ).flatten

  val moleculeHeading: Molecule => String = m => m.smiles

  val moleculeLines: Molecule => Seq[String] = m => m.name.toSeq ++ m.comment.toSeq

}
