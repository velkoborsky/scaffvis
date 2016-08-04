package scaffvis.stores

import java.util

import scaffvis.configuration.Locations
import org.mapdb._

/**
  * A concise imported form of PubChem Compound molecules. Contains the PubChem Compound IDs and corresponding SMILES.
  */
class PubchemStore(val readOnly: Boolean = true) extends MapDbStore {

  var compounds: BTreeMap[Long, String] = null

  //source file names
  var files: util.NavigableSet[String] = null

  lazy val dbFile = Locations.pubchemStore
  override val transactionsEnabled: Boolean = ! readOnly

  override def initialize(): Unit = {

    compounds = _db.treeMapCreate("compounds")
      .counterEnable()
      .keySerializer(BTreeKeySerializer.LONG)
      .valueSerializer(Serializer.STRING_ASCII)
      .makeOrGet[Long,String]()

    files = _db.treeSetCreate("files")
      .serializer(BTreeKeySerializer.STRING)
      .makeOrGet[String]()
  }

  override def cleanup(): Unit = {
    compounds = null
  }

  def getCompound(cid: Long): String = {
    compounds.get(cid)
  }

  def putCompound(cid: Long, smiles: String): Unit = {
    compounds.put(cid, smiles)
  }

}
