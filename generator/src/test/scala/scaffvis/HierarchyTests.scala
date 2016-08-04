package scaffvis

import scaffvis.chemistry.SmilesOps._
import scaffvis.chemistry.ScaffoldOps._
import org.scalatest.FlatSpec

class HierarchyTests extends FlatSpec {

  case class MoleculeHierarchy(
                                native: String,
                                ringsWithLinkersS: String,
                                ringsWithLinkers: String,
                                murckoRingsWithLinkers: String,
                                murckoRings: String,
                                oprea: String,
                                ringConnectivity1: String,
                                ringConnectivity2: String,
                                ringCount: Int
                              )

  def computeHierarchy(smiles: String): MoleculeHierarchy = {
    val native = smilesToMolecule(smiles)
    val nativeS = moleculeToSmiles(native, uniqueSmiles = true, stereoInformation = true)

    val rwls = nativeToRingsWithLinkers(native, clone = true)
    val rwlsS = moleculeToSmiles(rwls, uniqueSmiles = true, stereoInformation = true)
    val rwlS = moleculeToSmiles(rwls, uniqueSmiles = true, stereoInformation = false)

    val murckoRwl = ringsWithLinkers1toMurcko1(rwls, clone = true)
    val murckoRwlS = moleculeToSmiles(murckoRwl, uniqueSmiles = true, stereoInformation = false)

    val murckoR = murcko1to2(murckoRwl, clone = true)
    val murckoRS = moleculeToSmiles(murckoR, uniqueSmiles = true, stereoInformation = false)

    val oprea = murcko2toOprea1(murckoR, clone = true)
    val opreaS = moleculeToSmiles(oprea, uniqueSmiles = true, stereoInformation = false)

    val rc1 = oprea1toRingConnectivity1(oprea)
    val rc1S = moleculeToSmiles(rc1, uniqueSmiles = true, stereoInformation = false)

    val rc2 = ringConnectivity1to2(rc1, clone = true)
    val rc2S = moleculeToSmiles(rc2, uniqueSmiles = true, stereoInformation = false)

    val ringCount = ringConnectivityToRingCount(rc2)

    MoleculeHierarchy(native = nativeS, ringsWithLinkersS = rwlsS, ringsWithLinkers = rwlS,
      murckoRingsWithLinkers = murckoRwlS, murckoRings = murckoRS, oprea = opreaS,
      ringConnectivity1 = rc1S, ringConnectivity2 = rc2S, ringCount = ringCount)
  }

  "Computed hierarchy" should "be correct for Pubchem CID 20810691 (charged heteroatoms)" in {
    val pubchemSmiles = "C1=CC=C2C(=C1)C3=CC=CC=[N+]3[I-]2"
    val hierarchy = computeHierarchy(pubchemSmiles)
    val expected = MoleculeHierarchy(
      native                 = "[I-]1c2ccccc2-c2cccc[n+]12",
      ringsWithLinkersS       = "[I]1c2ccccc2C2=CC=CC=[N]12",
      ringsWithLinkers     = "[I]1c2ccccc2C2=CC=CC=[N]12",
      murckoRingsWithLinkers = "C1C2CCCCC2C2CCCCC12",
      murckoRings            = "C1C2CCCCC2C2CCCCC12",
      oprea                  = "C1C2C1C1CC21",
      ringConnectivity1          = "C=C=C",
      ringConnectivity2          = "CCC",
      ringCount              = 3
    )
    assertResult(expected)(hierarchy)
  }

  it should "be correct for Pubchem CID 10923938 (isotopes)" in {
    val pubchemSmiles = "[35S]1[35S][35S][35S][35S][35S][35S][35S]1"
    val hierarchy = computeHierarchy(pubchemSmiles)
    val expected = MoleculeHierarchy(
      native                 = "[35S]1[35S][35S][35S][35S][35S][35S][35S]1",
      ringsWithLinkersS       = "S1SSSSSSS1",
      ringsWithLinkers     = "S1SSSSSSS1",
      murckoRingsWithLinkers = "C1CCCCCCC1",
      murckoRings            = "C1CCCCCCC1",
      oprea                  = "C1CC1",
      ringConnectivity1          = "C",
      ringConnectivity2          = "C",
      ringCount              = 1
    )
    assertResult(expected)(hierarchy)
  }

  it should "be correct for Pubchem CID 23416909 (phosphorus with five bonds)" in {
    val pubchemSmiles = "C1OP23(OCP1CO2)OC(C(O3)(C(F)(F)F)C(F)(F)F)(C(F)(F)F)C(F)(F)F"
    val hierarchy = computeHierarchy(pubchemSmiles)
    val expected = MoleculeHierarchy(
      native                 = "FC(F)(F)C1(OP23(OC1(C(F)(F)F)C(F)(F)F)OCP(CO2)CO3)C(F)(F)F",
      ringsWithLinkersS       = "C1COP23(O1)OCP(CO2)CO3",
      ringsWithLinkers     = "C1COP23(O1)OCP(CO2)CO3",
      murckoRingsWithLinkers = "C1CC[C]23(C1)CCC(CC2)CC3",
      murckoRings            = "C1CC[C]23(C1)CCC(CC2)CC3",
      oprea                  = "C1C[C]112CC1C2",
      ringConnectivity1          = "C1C=C1",
      ringConnectivity2          = "C1CC1",
      ringCount              = 3
    )
    assertResult(expected)(hierarchy)
  }

}
