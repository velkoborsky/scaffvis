package scaffvis.stores

import better.files.File


/**
  * A class for simplified creation of ad hoc temporary databases for custom processing.
  */
class FileDb(val dbFile: File) extends MapDbStore {

  def db = _db

  override val readOnly: Boolean = false
  override val transactionsEnabled: Boolean = true

}

object FileDb {
  def apply(dbFile: File) = new FileDb(dbFile)

  def apply(path: String) = new FileDb(File(path))
}
