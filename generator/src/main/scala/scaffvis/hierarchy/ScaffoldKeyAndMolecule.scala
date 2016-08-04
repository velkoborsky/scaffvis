package scaffvis.hierarchy

import scaffvis.chemistry.ChemMolecule

case class ScaffoldKeyAndMolecule(key: String, molecule: Option[ChemMolecule] = None)
