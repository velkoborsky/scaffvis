package scaffvis.chemistry

import chemaxon.struc.AtomProperty.Radical
import chemaxon.struc.{MolAtom, MolBond, Molecule}

import scala.collection.mutable

/**
  * Static methods performing scaffold transformations.
  */
object ScaffoldOps {

  def nativeToRingsWithLinkers(molecule: ChemMolecule, clone: Boolean): ChemMolecule = {
    val m = if(clone) molecule.clone() else molecule
    removeSidechains(m)
    removeSpecificIsotopes(m)
    dischargeAndDeradicalize(m)
    fixValence(m)
    m

  }

  def ringsWithLinkers1toMurcko1(molecule: ChemMolecule): ChemMolecule = ringsWithLinkers1toMurcko1(molecule, true)

  def ringsWithLinkers1toMurcko1(molecule: ChemMolecule, clone: Boolean): ChemMolecule = {
    val m = if(clone) molecule.clone() else molecule
    carbonize(m)
    makeBondsSingle(m)
    dischargeAndDeradicalize(m)
    fixValence(m)
    m
  }

  def murcko1to2(molecule: ChemMolecule): ChemMolecule = murcko1to2(molecule, true)

  def murcko1to2(molecule: ChemMolecule, clone: Boolean): ChemMolecule = {
    val m = if(clone) molecule.clone() else molecule
    contractLinkers(m)
    m
  }

  def murcko2toOprea1(molecule: ChemMolecule): ChemMolecule = murcko2toOprea1(molecule, true)

  def murcko2toOprea1(molecule: ChemMolecule, clone: Boolean): ChemMolecule = {
    val m = if(clone) molecule.clone() else molecule
    minimizeRings(m)
    m
  }

  def oprea1toRingConnectivity1(molecule: ChemMolecule): ChemMolecule = {
    contractRings(molecule)
  }

  def ringConnectivity1to2(molecule: ChemMolecule): ChemMolecule = ringConnectivity1to2(molecule, true)

  def ringConnectivity1to2(molecule: ChemMolecule, clone: Boolean): ChemMolecule = {
    val m = if(clone) molecule.clone() else molecule
    makeBondsSingle(m)
    m
  }

  def ringConnectivityToRingCount(molecule: ChemMolecule): Int = {
    molecule.getAtomCount()
  }

  /**
    * remove all side chains, atoms not in any cycle
    *
    * @param m
    */
  def removeSidechains(m: ChemMolecule): Unit = {
    val leaves = new mutable.Queue[MolAtom]()
    leaves ++= m.getAtomArray.filter(a => a.getBondCount <= 1)
    while (leaves.nonEmpty) {
      val l = leaves.dequeue()
      val neighbours = l.getBondArray.map(b => b.getOtherAtom(l))
      m.removeAtom(l)
      leaves ++= neighbours.filter(a => a.getBondCount <= 1)
    }
  }

  val Carbon = 6
  //convert atoms to C, set charge to 0 and remove radicals
  def carbonize(m: ChemMolecule): Unit = {
    for(atom <- m.getAtomArray) {
      atom.setAtno(Carbon)
    }
  }

  def dischargeAndDeradicalize(m: ChemMolecule): Unit = {
    for(atom <- m.getAtomArray) {
      atom.setCharge(0)
      atom.setRadicalValue(Radical.NO_RADICAL)
    }
  }

  def removeSpecificIsotopes(m: ChemMolecule): Unit =  {
    for(atom <- m.getAtomArray) {
      atom.setMassno(0)
    }
  }

  //make all bonds single
  def makeBondsSingle(m: ChemMolecule): Unit = {
    m.getBondArray.foreach{b =>
      b.setType(1)
    }
  }

  def contractLinkers(m: ChemMolecule): Unit = {
    val linkers = new mutable.HashSet[MolAtom]()
    linkers ++= m.getAtomArray
      .filter(a => a.getBondCount == 2)
      .filter(a => ! m.isAtomInRing(a))
    while (linkers.nonEmpty) {
      //find a component
      val stack = new mutable.ArrayStack[MolAtom]() += linkers.head
      val component = new mutable.HashSet[MolAtom]()
      val border = new mutable.MutableList[MolAtom]()
      while (stack.nonEmpty) {
        val a = stack.pop()
        component += a
        linkers -= a
        a.getBondArray.map(b => b.getOtherAtom(a)).foreach{n =>
          if(! component.contains(n)) { //not visited yet
            if(linkers.contains(n)) {
              stack += n
            } else {
              border += n
            }
          }
        }
      }
      assert(border.length == 2)
      //remove component atoms from molecule and linkers set
      component.foreach{a =>
        m.removeAtom(a)
      }
      //contract
      m.add(new MolBond(border(0), border(1), 1))
    }
  }

  def minimizeRings(m: ChemMolecule): Unit = {
    for(a <- m.getAtomArray) {
      if(a.getBondCount == 2) {
        val neighbors = a.getBondArray.map(b => b.getOtherAtom(a))
        val n1 = neighbors(0)
        val n2 = neighbors(1)
        val connected = n1.getBondArray.exists(b => b.getOtherAtom(n1) == n2)
        if (! connected) {
          m.removeAtom(a)
          m.add(new MolBond(n1, n2, 1))
        }
      }
    }
  }

  //creates a new molecule, each ring replaced by one atom
  //atoms are connected iff rings were connected (by a bond not in any ring)
  //if the rings shared an bond, the bond is double
  def contractRings(m: ChemMolecule): ChemMolecule = {
    //get ring atoms
    val rings = m.getCSSR //or getSSSR?

    val connectedRings = mutable.HashSet.empty[(Int, Int)]
    val stronglyConnectedRings = mutable.HashSet.empty[(Int, Int)]

    //for each atom find all rings that contain it
    val atomRings = new mutable.LongMap((key) => mutable.TreeSet.empty[Int]) //default value, used for atoms with no rings
    for {
      (ring, ringIdx) <- rings.zipWithIndex
      atom <- ring
    } {
      atomRings.getOrElseUpdate(atom, mutable.TreeSet.empty[Int]) += ringIdx
    }

    //rings sharing an atom are connected
    for {
      (atom, rings) <- atomRings
      if rings.size > 1
      r1 <- rings
      r2 <- rings.from(r1).tail // strictly greater than r1
    } {
      connectedRings += ((r1, r2))
    }

    //get all bonds
    val connTab = m.getCtab //connTab[i][j] is the index of the j-th neighbor of the i-th atom
    val bonds = for {
        a1 <- 0 until connTab.length
        a2 <- connTab(a1).filter(_ > a1)
      } yield (a1, a2)


    val linkerBonds = mutable.ArrayBuffer.empty[(Int,Int)]

    for((a1, a2) <- bonds) {
      val rings1 = atomRings(a1)
      val rings2 = atomRings(a2)
      val bondRings = rings1 intersect rings2
      if(bondRings.isEmpty) { //if the bond belongs to no ring, it is a linker bond; we'll find linked components later
        linkerBonds += ((a1,a2))
      } else if (bondRings.size > 1) { //if it belongs to multiple rings, they are intersected
        for {
          r1 <- bondRings
          r2 <- bondRings.from(r1).tail
        } {
          stronglyConnectedRings += ((r1, r2))
        }
      }
      //else if the bond belongs to exactly one ring, do nothing - should not connect, would fail on e.g. gonane
    }

    //DFS on linker bonds -> components of connected atoms -> connect all corresponding rings
    val linkerConnTab: mutable.Map[Long, mutable.TreeSet[Int]] = mutable.LongMap.empty
    for ((a1, a2) <- linkerBonds) {
      linkerConnTab.getOrElseUpdate(a1, mutable.TreeSet.empty[Int]) += a2
      linkerConnTab.getOrElseUpdate(a2, mutable.TreeSet.empty[Int]) += a1
    }
    val linkerNodes = linkerConnTab.keySet.map(_.toInt)
    val components = mutable.ArrayBuffer.empty[mutable.TreeSet[Int]]
    val unvisited = mutable.TreeSet[Int](linkerNodes.toSeq: _*)
    while (unvisited.nonEmpty) {
      val stack = mutable.ArrayStack(unvisited.head)
      val component = mutable.TreeSet.empty[Int]
      def visit(a: Int) = {
        unvisited -= a
        component += a
        linkerConnTab(a).intersect(unvisited).foreach(stack.push) //add unvisited neighbours to the stack
      }
      while (stack.nonEmpty) {
        visit(stack.pop())
      }
      components += component
    }
    for {
      component <- components
      rings = component.flatMap(atomRings(_))
      r1 <- rings
      r2 <- rings.from(r1).tail
    } {
      connectedRings += ((r1, r2))
    }

    //construct new molecule
    val m2 = new ChemMolecule()
    val atoms: Array[MolAtom] = rings.map(_ => new MolAtom(Carbon)) //create a new node/atom for each ring
    atoms.foreach(m2.add)

    //add bonds
    for {(r1, r2) <- stronglyConnectedRings} {
      m2.add(new MolBond(atoms(r1), atoms(r2), 2))
    }
    for {(r1, r2) <- connectedRings.diff(stronglyConnectedRings)} {
      m2.add(new MolBond(atoms(r1), atoms(r2), 1))
    }

    //cleaning is expensive and not needed for database processing
    //Cleaner.clean(m2, 2, null)

    m2
  }

  def fixValence(m: ChemMolecule): Unit = {
    for(a <- m.getAtomArray) {
      a.getAtno match {
        case Carbon => a.setValenceProp(4)
        case _ => a.setValenceProp(a.getValence)
      }
    }
    m.valenceCheck()
    for(a <- m.getAtomArray) {
      a.setValenceProp(-1)
    }
  }

}
