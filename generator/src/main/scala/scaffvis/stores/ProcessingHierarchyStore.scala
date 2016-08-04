package scaffvis.stores

import scaffvis.configuration.Locations
import org.mapdb.BTreeMap

/**
  * A helper hierarchy, used at the processing time.
  */
class ProcessingHierarchyStore(val readOnly: Boolean = true) extends MapDbStore {

  override protected lazy val dbFile = Locations.processingHierarchyStore
  override val transactionsEnabled: Boolean = ! readOnly

  val bottomLvl = 8
  val topLvl = 0

  def subtreeSizeMap(level: Int): BTreeMap[String, Int] = {
    require(topLvl <= level && level <= bottomLvl)
    subtreeSizeMaps(level)
  }

  def parentMap(level: Int): BTreeMap[String, String] = {
    require(topLvl <= level && level <= bottomLvl)
    parentMaps(level)
  }

  def processingErrorMap(level: Int): BTreeMap[String, String] = {
    require(topLvl <= level && level <= bottomLvl)
    processingErrorMaps(level)
  }

  private val subtreeSizeMaps: Array[BTreeMap[String, Int]] = Array.ofDim(bottomLvl + 1)
  private val parentMaps: Array[BTreeMap[String, String]] = Array.ofDim(bottomLvl + 1)

  var pubchemProcessingErrorMap: BTreeMap[Long, String] = _
  private val processingErrorMaps: Array[BTreeMap[String, String]] = Array.ofDim(bottomLvl + 1)

  override def initialize(): Unit = {
    for (lvl <- topLvl to bottomLvl) {
      subtreeSizeMaps(lvl) = getStringIntMap(s"subtreeSize_lvl${lvl}")
      parentMaps(lvl) = getStringStringAsciiMap(s"parent_lvl${lvl}")
      processingErrorMaps(lvl) = getStringStringMap(s"processingError_lvl${lvl}")
    }
    pubchemProcessingErrorMap = getLongStringMap("pubchemProcessingError")
  }

  override def cleanup(): Unit = {
    for (lvl <- topLvl to bottomLvl) {
      subtreeSizeMaps(lvl) = null
      parentMaps(lvl) = null
      processingErrorMaps(lvl) = null
    }
    pubchemProcessingErrorMap = null
  }
}
