package scaffvis.client.store.serializer

import java.nio.ByteBuffer
import java.util

import boopickle.Default._

/**
  * Saves and loads application model/state to and from byte array
  */
object BooPickleSerializer extends Serializer[Array[Byte]] {

  protected def serialize(data: SaveFileFormat) = byteBufferToArray(serializeBB(data))

  protected def deserialize(bytes: Array[Byte]) = deserializeBB(ByteBuffer.wrap(bytes))

  private def serializeBB(data: SaveFileFormat): ByteBuffer = {
    Pickle.intoBytes[SaveFileFormat](data)
  }

  private def deserializeBB(bb: ByteBuffer): SaveFileFormat = {
    Unpickle[SaveFileFormat].fromBytes(bb)
  }

  private def byteBufferToArray(bb: ByteBuffer): Array[Byte] = {
    if (bb.hasArray) {
      val offset = bb.arrayOffset
      val position = bb.position()
      val limit = bb.limit()
      util.Arrays.copyOfRange(bb.array(), offset + position, offset + limit)
    } else {
      val remaining = bb.remaining()
      val dst = Array.ofDim[Byte](remaining)
      bb.get(dst)
      dst
    }
  }

}

