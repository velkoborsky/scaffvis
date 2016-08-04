package scaffvis.stores

import scaffvis.configuration.Locations
import scaffvis.hierarchy.ScaffoldHierarchy
import scaffvis.shared.model._
import org.mapdb.BTreeMap

/**
  * The final background hierarchy, implementing the ScaffoldHierarchy API.
  */
class ScaffoldHierarchyStore(val readOnly: Boolean = true) extends MapDbStore with ScaffoldHierarchy {

  import ScaffoldHierarchyStore.RawScaffold

  override protected lazy val dbFile = Locations.scaffoldHierarchyStore
  override val transactionsEnabled: Boolean = ! readOnly

  val topLvl = HierarchyLevels.topLvl
  val bottomLvl = HierarchyLevels.bottomLvl

  def scaffoldToIdMap(level: Int): BTreeMap[String, Int] = {
    require(topLvl <= level && level <= bottomLvl)
    scaffoldToIdMaps(level)
  }

  private val scaffoldToIdMaps: Array[BTreeMap[String, Int]] = Array.ofDim(bottomLvl + 1)

  var idToScaffoldMap: BTreeMap[Int, RawScaffold] = _
  var parentMap: BTreeMap[Int, Int] = _
  var childrenMap: BTreeMap[Int, Array[Int]] = _

  override def initialize(): Unit = {
    for (lvl <- topLvl to bottomLvl) {
      scaffoldToIdMaps(lvl) = getStringIntMap(s"scaffoldToId_lvl${lvl}")
    }
    idToScaffoldMap = getIntGenericMap[RawScaffold]("idToScaffold")
    parentMap = getIntIntMap("parent")
    childrenMap = getIntIntArrayMap("children")
  }

  override def cleanup(): Unit = {
    for (lvl <- topLvl to bottomLvl) {
      scaffoldToIdMaps(lvl) = null
    }
    idToScaffoldMap = null
    parentMap = null
    childrenMap = null
  }


  override def get(scaffoldId: ScaffoldId): Scaffold = {
    val rawScaffold = idToScaffoldMap.get(scaffoldId)
    rawScaffoldToScaffold(rawScaffold, scaffoldId)
  }

  override def getId(level: Int, key: String): Option[ScaffoldId] = {
    val scaffoldId = scaffoldToIdMap(level).get(key)
    if (scaffoldId == 0) //MapDB Int map is not 0-safe
      None
    else
      Option(scaffoldId)
  }

  override def children(parentId: ScaffoldId): Seq[Scaffold] = {
    val childrenIds = childrenMap.getOrDefault(parentId, Array.empty)
    childrenIds.map(get)
  }

  override def parentId(childId: ScaffoldId): Option[ScaffoldId] = {
    val parentId = parentMap.get(childId)
    if (parentId == 0) //MapDB Int map is not 0-safe
      None
    else
      Some(parentId)
  }

  override def parentId_!(childId: ScaffoldId): ScaffoldId = {
    val parentId = parentMap.get(childId)
    if (parentId == 0) //MapDB Int map is not 0-safe
      throw new NoSuchElementException
    else
      parentId
  }

  private def rawScaffoldToScaffold(rawScaffold: RawScaffold, scaffoldId: ScaffoldId): Scaffold = rawScaffold match {
    case RawScaffold(0, _, _) =>
      RootScaffold
    case RawScaffold(1, key, subtreeSize) =>
      RingCountScaffold(id = scaffoldId, level = 1, ringCount = key.toInt, subtreeSize = subtreeSize)
    case RawScaffold(level, key, subtreeSize) =>
      SmilesScaffold(id = scaffoldId, level = level, smiles = key, subtreeSize = subtreeSize)
  }
}

object ScaffoldHierarchyStore {

  case class RawScaffold(level: Byte, key: String, subtreeSize: Int)

}