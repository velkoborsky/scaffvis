package scaffvis.client.components.common

import scaffvis.ReusableComputation
import scaffvis.client.store.model.Molecules
import scaffvis.shared.model._

/**
  * Reusable expensive computations.
  */
object ReusableCmps {

  def selectedMoleculesInSubtree(molecules: Molecules, scaffold: Scaffold): Set[Molecule] =
    _selectedMoleculesInSubtree((molecules, scaffold))

  private val _selectedMoleculesInSubtree = {
    type I = (Molecules, Scaffold)
    type K = (Set[MoleculeId], Scaffold) //selected, scaffold
    type O = Set[Molecule]

    ReusableComputation[I, K, O](
      f = {
        case (molecules, scaffold) =>
          molecules.selected.map(molecules.get_!).filter(_.isInSubtree(scaffold))
      },
      extractKey = {
        case (molecules, scaffold) =>
          (molecules.selected, scaffold)
      },
      eq = (k1, k2) => {
        (k1._1 eq k2._1) && //selected by ref
          (k1._2 == k2._2) //scaffold by val
      }
    )
  }

}
