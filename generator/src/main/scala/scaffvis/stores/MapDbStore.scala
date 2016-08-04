package scaffvis.stores

import better.files.File
import org.mapdb._

/**
  * A skeleton of a MapDB store.
  */
trait MapDbStore {

  protected var _db : DB = _

  val readOnly: Boolean
  val transactionsEnabled: Boolean

  protected def dbFile: File

  def metadata = _metadata
  private var _metadata: BTreeMap[String,String] = _

  def isOpen = _isOpen
  private var _isOpen: Boolean = false

  /**
    * called after the database is opened
    */
  def initialize() = ()

  /**
    * called before the datbase is closed
    */
  def cleanup() = ()

  def open(): Unit = {
    if(_db == null) {
      dbFile.parent.createDirectories()

      println(s"Opening MapDB file ${dbFile.pathAsString}")
      val dbMaker = DBMaker
        .fileDB(dbFile.toJava)
        .fileMmapEnableIfSupported()
        .fileMmapCleanerHackEnable()
        .compressionEnable()

      if(readOnly) {
        dbMaker.readOnly()
      }

      if(! transactionsEnabled) {
        dbMaker.transactionDisable()
      }

      _db = dbMaker.make()

      _metadata = _db.treeMapCreate("_metadata")
        .keySerializer(BTreeKeySerializer.STRING)
        .valueSerializer(Serializer.STRING)
        .makeOrGet()

      initialize()
      _isOpen = true
    }
  }

  /**
    * Must be closed before JVM exits.
    */
  def close(): Unit = {
    _isOpen = false
    if(_db != null) {
      cleanup()
      println(s"Closing MapDB file ${dbFile.pathAsString}")
      _db.close()
      if(_db.isClosed) {
        _db = null
        _metadata = null
      }
    }
  }

  def commit() : Unit = {
    _db.commit()
  }

  private def getTreeMap[K,V](name: String, keySerializer: BTreeKeySerializer[_,_], valueSerializer: Serializer[_]): BTreeMap[K,V] = {
    _db.treeMapCreate(name)
      .counterEnable()
      .keySerializer(keySerializer)
      .valueSerializer(valueSerializer)
      .makeOrGet()
  }


  protected def getStringIntMap(name: String): BTreeMap[String,Int] = getTreeMap(name, BTreeKeySerializer.STRING, Serializer.INTEGER)

  protected def getStringStringAsciiMap(name: String): BTreeMap[String,String] = getTreeMap(name, BTreeKeySerializer.STRING, Serializer.STRING_ASCII)

  protected def getStringStringMap(name: String): BTreeMap[String,String] = getTreeMap(name, BTreeKeySerializer.STRING, Serializer.STRING)

  protected def getIntIntMap(name: String): BTreeMap[Int,Int] = getTreeMap(name, BTreeKeySerializer.INTEGER, Serializer.INTEGER)

  protected def getIntIntArrayMap(name: String): BTreeMap[Int,Array[Int]] = getTreeMap(name, BTreeKeySerializer.INTEGER, Serializer.INT_ARRAY)

  protected def getIntGenericMap[V](name: String): BTreeMap[Int, V] = _db.treeMapCreate(name).counterEnable().keySerializer(BTreeKeySerializer.INTEGER).makeOrGet()

  protected def getLongStringMap(name: String): BTreeMap[Long,String] = getTreeMap(name, BTreeKeySerializer.LONG, Serializer.STRING)

}
