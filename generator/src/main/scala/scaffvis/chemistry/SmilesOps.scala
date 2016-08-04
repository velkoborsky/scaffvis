package scaffvis.chemistry

import chemaxon.formats.{MolExporter, MolImporter}

/**
  * Static methods performing conversions between SMILES and in-memory representations of a molecule.
  */
object SmilesOps {

  def canonicalize(smiles: String) : String = moleculeToSmiles(smilesToMolecule(smiles))

  def smilesToMolecule(smiles: String): ChemMolecule = {
    if(smiles.isEmpty()) {
      new ChemMolecule()
    } else {
      MolImporter.importMol(smiles, "smiles")
    }
  }

  /**
    * @see https://marvin-demo.chemaxon.com/marvin/help/formats/smiles-doc.html
    * @param molecule
    * @param uniqueSmiles Marvin: Aan approximation to make the SMILES string as absolute (unique for isomeric structures) as possible. In this case the form of any aromatic compound is aromatized before SMILES export. It is possible to include chirality into graph invariants. This option must be used with care since for molecules with numerous chirality centres the canonicalization can be very CPU demanding [2].
    * @param removeExplicitHAtoms Marvin: Remove explicit Hydrogen atoms.
    * @param stereoInformation Marvin: Do/do not include chirality (parity) and double bond stereo (cis/trans) information.
    * @return
    */
  def moleculeToSmiles(molecule: ChemMolecule, uniqueSmiles: Boolean = false, removeExplicitHAtoms: Boolean = true,
                       stereoInformation: Boolean = true, chemaxonSmiles: Boolean = false): String = {
    val options = (if(uniqueSmiles) "u" else "") +
      (if(removeExplicitHAtoms) "-H" else "") +
      (if(stereoInformation) "" else "0")
    val baseFormat = if(chemaxonSmiles) "cxsmiles" else "smiles"
    val formatWithOptions = if(options.isEmpty) baseFormat else s"$baseFormat:$options"
    MolExporter.exportToFormat(molecule, formatWithOptions)
  }
}
