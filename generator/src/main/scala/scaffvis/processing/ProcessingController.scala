package scaffvis.processing

import scaffvis.stores.MapDbStore

/**
  * Stores last processed key into store's metadata.
  */
abstract class ProcessingController[KeyType] {
  /**
    * Get the last processed key.
 *
    * @return
    */
  def lastProcessedKey: Option[KeyType]

  /**
    * Save the last processed key and commit the changes in the store.
 *
    * @param lastProcessedKey
    */
  def commit(lastProcessedKey: KeyType)
}

object ProcessingController {

  def withStringKey(store: MapDbStore, name: String) = new ProcessingController[String] {
    def lastProcessedKey = {
      if(store.metadata.containsKey(name))
        Some(store.metadata.get(name))
      else
        None
    }
    def commit(lastProcessedKey: String): Unit = {
      store.metadata.put(name, lastProcessedKey)
      store.commit()
    }
  }

  def withIntKey(store: MapDbStore, name: String) = new ProcessingController[Int] {
    def lastProcessedKey = {
      if(store.metadata.containsKey(name))
        Some(store.metadata.get(name).toInt)
      else
        None
    }
    def commit(lastProcessedKey: Int): Unit = {
      store.metadata.put(name, lastProcessedKey.toString)
      store.commit()
    }
  }

  def withLongKey(store: MapDbStore, name: String) = new ProcessingController[Long] {
    def lastProcessedKey = {
      if(store.metadata.containsKey(name))
        Some(store.metadata.get(name).toLong)
      else
        None
    }
    def commit(lastProcessedKey: Long): Unit = {
      store.metadata.put(name, lastProcessedKey.toString)
      store.commit()
    }
  }
}