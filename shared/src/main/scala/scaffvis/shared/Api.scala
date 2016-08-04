package scaffvis.shared

import scaffvis.shared.model._

trait Api {

  def getMoleculeSvg(smiles: Seq[String]): Seq[String]

  def getScaffoldSvg(ids: Seq[ScaffoldId]): Seq[String]

  /**
    *  Scaffold hierarchy - get children of a scaffold
    */
  def getChildren(scaffoldId: ScaffoldId): Seq[Scaffold]

  /**
    * Returns a scaffolds and his ancestors up to Root
    */
  def getScaffold(scaffoldId: ScaffoldId): Seq[Scaffold]

  /**
    * Load (and preprocess) dataset
    */

  def loadFromSmiles(smiles: Seq[String]): Seq[Molecule]

  def loadFromFile(fileContent: Array[Byte]): Seq[Molecule]
}
