package scaffvis.hierarchy

import scaffvis.shared.model.HierarchyLevels
import scaffvis.chemistry.{ChemMolecule, ScaffoldOps, SmilesOps}

/**
  * This object contains the chemical definition of the hierarchy structure.
  *
  * The rest of the definition is in the object Levels in the project shared as it needs to be accessible from the client.
  *
  * Change these two classes to use an alternative hierarchy.
  */
object HierarchyTransformations {

  /**
    * Provides a transformation from a given level up.
    */
  def fromLevel(level: Int) = levelTransformations(level)

  /**
    * Define how to transfer a scaffold from a level to a higher level.
    */
  private val levelTransformations: Array[ScaffoldKeyAndMolecule => ScaffoldKeyAndMolecule] = {

    import HierarchyLevels._
    import ScaffoldOps._
    val a = Array.ofDim[ScaffoldKeyAndMolecule => ScaffoldKeyAndMolecule](count)
    a(ringsWithLinkersStereo) = wrap(identity) //identity but removes stereo from smiles
    a(ringsWithLinkers      ) = wrap(ringsWithLinkers1toMurcko1(_, clone = false))
    a(murckoRingsWithLinkers) = wrap(murcko1to2(_, clone = false))
    a(murckoRings           ) = wrap(murcko2toOprea1(_, clone = false))
    a(oprea                 ) = wrap(oprea1toRingConnectivity1)
    a(ringConnectivityExtended          ) = wrap(ringConnectivity1to2(_, clone = false))
    a(ringConnectivity) = {case ScaffoldKeyAndMolecule(key, moleculeOpt) =>
      val molecule: ChemMolecule = moleculeOpt.getOrElse(SmilesOps.smilesToMolecule(key))
      val ringCount = ringConnectivityToRingCount(molecule)
      ScaffoldKeyAndMolecule(key = "%03d".format(ringCount), molecule = None) //zero padded for better lexicographic sorting
    }
    a(ringCount             ) = {_ => ScaffoldKeyAndMolecule(key = "ROOT", molecule = None)}
    a(root                  ) = {_ => throw new RuntimeException("Already at the top level")}
    a
  }


  /**
    * Provides a transformation from a native molecule to the bottom level scaffold.
 *
    * @param molecule
    * @param clone
    * @return
    */
  def fromNative(molecule: ChemMolecule, clone: Boolean = true): ScaffoldKeyAndMolecule = {
    val transformedMolecule = ScaffoldOps.nativeToRingsWithLinkers(molecule, clone = clone)
    val transformedSmiles = SmilesOps.moleculeToSmiles(transformedMolecule, uniqueSmiles = true, stereoInformation = true)
    ScaffoldKeyAndMolecule(key = transformedSmiles, molecule = Some(transformedMolecule))
  }

  /**
    * Wraps ChemMolecule transformation into ScaffoldKeyAndMolecule transformation.
 *
    * @param transformation
    * @return
    */
  def wrap(transformation: (ChemMolecule) => ChemMolecule): ScaffoldKeyAndMolecule => ScaffoldKeyAndMolecule = {
    case ScaffoldKeyAndMolecule(key, moleculeOpt) =>
      val molecule: ChemMolecule = moleculeOpt.getOrElse(SmilesOps.smilesToMolecule(key))
      val transformedMolecule = transformation(molecule)
      val transformedSmiles = SmilesOps.moleculeToSmiles(transformedMolecule, uniqueSmiles = true, stereoInformation = false)
      ScaffoldKeyAndMolecule(key = transformedSmiles, molecule = Some(transformedMolecule))
  }

}
