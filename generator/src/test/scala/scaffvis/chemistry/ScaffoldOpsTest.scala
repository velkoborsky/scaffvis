package scaffvis.chemistry

import chemaxon.struc.Molecule
import org.scalatest.FlatSpec

class ScaffoldOpsTest extends FlatSpec {

  val contractRings = smilesFuncAdaptor(ScaffoldOps.contractRings)

  "ScaffoldOps.contractRings" should "transform cyclopropane to one node" in {
    assertResult("C")(contractRings("C1CC1"))
  }

  it should "transform benzene to one node" in {
    assertResult("C")(contractRings("C1=CC=CC=C1"))
  }

  it should "transform anthracene into a path of three strongly connected nodes" in {
    assertResult("C=C=C")(contractRings("C1=CC2=CC3=C(C=CC=C3)C=C2C=C1"))
  }

  it should "transform gonane into a path of four strongly connected nodes" in {
    /*

           __                    __
       ___/  \___            ___/  \___
    __/          \___    ___/          \___
  _/                 \__/                  \__
  |                    |                     |
  |                    |                     |
  |                    |                     |
  |                    |                     |
  |______________________                    __                    __
                         \___            ___/  \___            ___/  \___
                             \___    ___/          \___    ___/          \__
                                 \__/                  \__/                 \_
                                  |                     |                    |
                                  |                     |                    |
                                  |                     |                    |
                                  |                     |                    |
                                  |                     |                    |
                                  _                    __                   _|
                                   \___            ___/  \___           ___/
                                       \___    ___/          \___    __/
                                           \__/                  \__/














     */
    assertResult("C=C=C=C")(contractRings("C1CC2CCC3C(CCC4CCCCC34)C2C1"))
  }

  it should "transform phenalene into a three mutually strongly connected nodes" in {
    /*

                                       __
                                   ___/  \___
                                __/   _      \___
                            ___/   __/           \__
                        ___/    __/                 \___
                     __/     __/                        \_
                     |    __/                            |
                     |                                |  |
                     |                                |  |
                     |                                |  |
                     |                                |  |
                     |                                |  |
                     |                                |  |
                     |                                |  |
                     |   __                             ___
                   ____    \___                     ___/   \___
               ___/    \___    \___             ___/           \___
          ____/            \____   \___      __/                   \___
      ___/                      \___     ___/                          \___
   __/                              \___/                                  \_
   |                                   |                                    |
   |                                   |                                    |
   |  |                                |                                |   |
   |  |                                |                                |   |
   |  |                                |                                |   |
   |   \                               |                                |   |
   |   |                               |                                |   |
   |   |                               |                                |   |
   |   |                           _   |                                    |
   |_                           __/    _                                   _|
     \___                   ___/   ___/ \___                           ___/
         \___            __/    __/         \___                   ___/
             \__      __/   ___/                \___            __/
                \___    ___/                        \___    ___/
                    \__/                                \__/
     */
    assertResult("C=1=C=C=1")(contractRings("C1C=CC2=CC=CC3=C2C1=CC=C3"))
  }

  it should "transform empty molecule into empty molecule" in {
    assertResult("")(contractRings(""))
  }

  it should "transform phenylnaphthalene to a node connected to two strongly connected nodes" in {
    /*

                                                  ____
                                              ___/   _\___
                                          ___/        \_  \___
                                        _/              \__   \___
                                        |                  \__   |
                                        | |                      |
                                        | |                      |
                                        | |                      |
                                        | |                      |
                                        | |                      |
            ____                      __|__                     __
        ___/   _\___              ___/     \___             ___/ _\___
    ___/        \__ \___      ___/             \___     ___/      \__ \___
  _/               \__  \____/                     \___/             \__  \___
  |                   \__  |                        |                   \__  |
  | |                      |                        |  |                     |
  | |                      |                        |  |                     |
  | |                      |                        |  |                     |
  | |                      |                        |  |                     |
  | |                  __  |                        |  |                 __  |
  |__              ___/   __                        |__              ___/    _
     \___      ___/   ___/                             \___      ___/    ___/
         \___     ___/                                     \___      ___/
             \___/                                             \____/











     */
    assertResult("CC=C")(contractRings("C1=CC=C(C=C1)C2=CC=CC3=CC=CC=C32")) //cid 11795
  }

  it should "transform biphenyl into two connected nodes" in {
    assertResult("CC")(contractRings("C1=CC=C(C=C1)C2=CC=CC=C2")) //cid 7095
  }

  it should "transform complicated cycle system" in {
    /*

                                                           _________
                                              ____________/        \_
                                      _______/     \_             /  \_
                _____________________/               \_          /     \
            ___/ |                |                    \        /       \_
        ___/     |                |                     \_     /          \_
    ___/         |                |                       \_  /       ______\_
  __             |                |                         _________/       \
    \___         |                |                        _/
        \___     |                |                      _/
            \___ |                |                     /
                \______________________               _/
                                       \_______     _/
                                               \___/
                                                  /\
                                                   |
                                                    \
                                                    |
                                                     \
                                                      \
                                                      |
                                                       _
                                                       |\_
                                                      /   \__
                                                      |      \__
                                                     /          \_
                                                     |          __\__
                                                    /   _______/   |
                                                    |__/           /
                                                      \_          |
                                                        \__       /
                                                           \_    |
                                                             \_  /
                                                               \_

     */
    assertResult("C=C[C]1(=C=C1)=C=C")(contractRings("C1C2C1C2C1C2C3CC3C2C2C3CC123"))
    //or would we prefer "C=C[C](=C=C)=C=C"? how to define it?
  }

  it should "transform 'butterfy' (spiropentane) into two connected nodes" in {
    assertResult("CC")(contractRings("C1CC11CC1"))
  }

  it should "transform two directly connected rings (bicyclopropane) into two connected nodes" in {
    assertResult("CC")(contractRings("C1CC1C1CC1"))
  }

  it should "transform two rings connected by a path into two connected nodes" in {
    assertResult("CC")(contractRings("C(C1CC1)C1CC1"))
  }

  it should "transform three rings mutually connected by a path into a triangle" in {
    assertResult("C1CC1")(contractRings("C1CC1C(C1CC1)C1CC1"))
  }

  it should "transform four rings mutually connected by a path into a K4" in {
    assertResult("C12C3C1C23")(contractRings("C1CC1C(C1CC1)(C1CC1)C1CC1"))
  }

  val dischargeAndDeradicalize = smilesProcAdaptor(ScaffoldOps.dischargeAndDeradicalize)

  "ScaffoldOps.dischargeAndDeradicalize" should "remove radicals in [CH]1[C]2CCCCC2C2CCCCC12" in {
    assertResult("C1C2CCCCC2C2CCCCC12")(dischargeAndDeradicalize("[CH]1[C]2CCCCC2C2CCCCC12"))

  }

  def smilesFuncAdaptor(transformation: (Molecule) => Molecule): (String) => String = {
    (SmilesOps.smilesToMolecule _).andThen(transformation).andThen(SmilesOps.moleculeToSmiles(_, uniqueSmiles = true))
  }

  def smilesProcAdaptor(transformation: (Molecule) => Unit): (String) => String = {
    (smiles: String) => {
      val molecule = SmilesOps.smilesToMolecule(smiles)
      transformation(molecule)
      SmilesOps.moleculeToSmiles(molecule, uniqueSmiles = true)
    }
  }

  "ScaffoldOps.ringsWithLinkers1toMurcko1" should
    "properly transform Pubchem CID 6395538 [AsH-]1[As][AsH-][As][As][As][AsH-][As]1 to a simple carbon cycle" in {
    assertResult("C1CCCCCCC1"){
      smilesFuncAdaptor(ScaffoldOps.ringsWithLinkers1toMurcko1)("[AsH-]1[As][AsH-][As][As][As][AsH-][As]1")
    }

  }

}
