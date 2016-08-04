package scaffvis.client

import scaffvis.shared.model.{Molecule, MoleculeId, Scaffold, ScaffoldId}

package object components {
  type MolPred = Molecule => Boolean
  type MolIdPred = MoleculeId => Boolean
  type ScaPred = Scaffold => Boolean
  type ScaIdPred = ScaffoldId => Boolean
}
