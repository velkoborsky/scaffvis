package scaffvis.client.store.serializer

import upickle.default._

/**
  * Saves and loads application model/state to and from string
  */
object UPickleSerializer extends Serializer[String] {

  private val UTF8 = "UTF-8"
  protected def serialize(data: SaveFileFormat): String = {
    write[SaveFileFormat](data) //getBytes(UTF8)
  }

  protected def deserialize(str: String): SaveFileFormat = {
    read[SaveFileFormat](str)
  }

}

