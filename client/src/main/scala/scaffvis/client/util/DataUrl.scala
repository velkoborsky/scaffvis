package scaffvis.client.util

import org.scalajs.dom.{Blob, BlobPropertyBag, URL}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.URIUtils
import scala.scalajs.js.typedarray.Uint8Array

object DataUrl {

  val contentType = "application/octet-stream"
  private val UTF8 = "UTF-8"

  def dataUrl(data: String): String = s"data:$contentType,${URIUtils.encodeURIComponent(data)}"

  def blobUrl(data: Array[Byte]): String = {
    val uint8Arr = new Uint8Array(data.toJSArray)
    val blob = new Blob(js.Array(uint8Arr), BlobPropertyBag(`type` = contentType))
    URL.createObjectURL(blob)
  }

  def blobUrl(data: String): String = blobUrl(data.getBytes(UTF8))

}

@js.native
object URL extends URL
