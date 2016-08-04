package scaffvis.chemistry

import chemaxon.calculations.clean.Cleaner
import chemaxon.struc.{MolAtom, MolBond, Molecule, StereoConstants}
import org.scalatest.FlatSpec

class SmilesOpsTest extends FlatSpec {

  "SmilesOps moleculeToSmiles" should "return correct smiles for carbon monoxide" in {
    val smiles = SmilesOps.moleculeToSmiles(carbonMonoxide)
    val expected = "C=O"

    assertResult(expected)(smiles)
  }

  it should "return correct smiles for butene" in {
    for(
      u <- Seq(true, false);
      r <- Seq(true, false)
    ) {
      assertResult("""C\C=C\C""")(SmilesOps.moleculeToSmiles(butene(   ), uniqueSmiles = u, removeExplicitHAtoms = r))
      assertResult("""C\C=C/C""")(SmilesOps.moleculeToSmiles(butene('C'), uniqueSmiles = u, removeExplicitHAtoms = r))
      assertResult("""C\C=C\C""")(SmilesOps.moleculeToSmiles(butene('T'), uniqueSmiles = u, removeExplicitHAtoms = r))
      assertResult("""CC=CC"""  )(SmilesOps.moleculeToSmiles(butene(   ), stereoInformation = false, uniqueSmiles = u, removeExplicitHAtoms = r))
      assertResult("""CC=CC"""  )(SmilesOps.moleculeToSmiles(butene('C'), stereoInformation = false, uniqueSmiles = u, removeExplicitHAtoms = r))
      assertResult("""CC=CC"""  )(SmilesOps.moleculeToSmiles(butene('T'), stereoInformation = false, uniqueSmiles = u, removeExplicitHAtoms = r))
    }
  }

  def carbonMonoxide: Molecule = {
    val m = new Molecule()

    val C = new MolAtom(6)
    val O = new MolAtom(8)

    m.add(C)
    m.add(O)

    val bond = new MolBond(C, O, 2)
    m.add(bond)

    m
  }

  /**
    *
    * @param stereo 'C'is, 'T'rans or 'W'iggly
    * @return
    */
  def butene(stereo: Char = 'W'): Molecule = {
    val m = new Molecule()

    val C = (0 to 3).map(_ => new MolAtom(6)).toArray
    C.foreach(m.add(_))

    val b = new MolBond(C(1), C(2), 2)
    m.add(b)

    m.add(new MolBond(C(0), C(1)))
    m.add(new MolBond(C(2), C(3)))

    import MolBond.STEREO_MASK
    import StereoConstants.{CIS, TRANS}

    assertResult(0)(b.getFlags & STEREO_MASK)

    stereo match {
      case 'C' =>
        b.setFlags(CIS, STEREO_MASK)
        assertResult(CIS)(b.getFlags & STEREO_MASK)
      case 'T' =>
        b.setFlags(TRANS, STEREO_MASK)
        assertResult(TRANS)(b.getFlags & STEREO_MASK)
      case 'W' =>
        ;
    }

    Cleaner.clean(m, 2, null);

    assertResult(0)(b.getFlags & STEREO_MASK)

    m
  }


}
