package scaffvis.client.util

import org.scalajs.dom.{Event, File, FileReader, UIEvent}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.typedarray.{ArrayBuffer, Int8Array}

object FileIO {

  def fileToByteArray(file: File): Future[Array[Byte]] = {

    val reader = new FileReader()
    val promise = Promise[Array[Byte]]

    reader.onload = (e: UIEvent) => {
      val buffer = reader.result.asInstanceOf[ArrayBuffer] //because we use readAsArrayBuffer later
      val jsTypedArr = new Int8Array(buffer) //scala byte is signed
      val byteArr: Array[Byte] = jsTypedArr.toArray
      promise.success(byteArr)
    }

    reader.onerror = (e: Event) => {
      val msg = reader.error.name
      promise.failure(new Exception(msg))
    }

    reader.onabort = (e: Event) => {
      val msg = "Aborted."
      promise.failure(new Exception(msg))
    }

    reader.readAsArrayBuffer(file)

    promise.future
  }

}
