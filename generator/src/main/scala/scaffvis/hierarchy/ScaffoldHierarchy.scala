package scaffvis.hierarchy

import scaffvis.chemistry.ChemMolecule
import scaffvis.shared.model._

/**
  * Defines a the basic API to access a processed hierarchy
  */
trait ScaffoldHierarchy {

  def topLvl: Int
  def bottomLvl: Int

  def get(scaffoldId: ScaffoldId): Scaffold
  def getId(level: Int, key: String): Option[ScaffoldId]
  def children(parentId: ScaffoldId): Seq[Scaffold]
  def parentId(childId: ScaffoldId): Option[ScaffoldId]
  def parentId_!(childId: ScaffoldId): ScaffoldId

  def root: Scaffold = RootScaffold

  def children(parent: Scaffold): Seq[Scaffold] = children(parent.id)
  def parent(child: Scaffold): Option[Scaffold] = parentId(child.id).map(get)

  /**
    * @return Array: LevelId -> ScaffoldId, including self
    */
  def scaffoldAncestors(scaffoldId: ScaffoldId, scaffoldLvl: Int): Array[ScaffoldId] = {

    var id = scaffoldId
    var lvl = scaffoldLvl
    val arr = Array.ofDim[ScaffoldId](lvl + 1)

    @inline def storeToArray():Unit = arr(lvl) = id

    storeToArray()
    while(lvl > topLvl) {
      id = parentId_!(id)
      lvl -= 1
      storeToArray()
    }

    arr
  }

  def scaffoldAncestors(scaffoldIdAndLevel: (ScaffoldId,Int)): Array[ScaffoldId]  =
    scaffoldAncestors(scaffoldIdAndLevel._1, scaffoldIdAndLevel._2)

  def scaffoldAncestors(scaffoldId: ScaffoldId): Array[ScaffoldId] =
    scaffoldAncestors(scaffoldId, get(scaffoldId).level)


  def findLowestScaffold(molecule: ChemMolecule, clone: Boolean = true): (ScaffoldId, Int) = {
    var lvl = bottomLvl
    var scaffold = HierarchyTransformations.fromNative(molecule, clone = clone)
    var currentId: Option[ScaffoldId] = None
    def updateId() = {
      currentId = getId(lvl, scaffold.key)
    }

    //find in hierarchy
    while({updateId(); currentId.isEmpty && lvl >= topLvl}) {
      //not found on this level -> go to the higher level
      scaffold = HierarchyTransformations.fromLevel(lvl)(scaffold)
      lvl -= 1
    }
    (currentId.get, lvl)
  }

  def findAllScaffoldsAndDestroyMolecule(molecule: ChemMolecule): Array[ScaffoldId] =
    scaffoldAncestors(findLowestScaffold(molecule, clone = false))

}
