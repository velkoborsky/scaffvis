package scaffvis.client.util

import japgolly.scalajs.react.Callback
import org.scalajs.dom.window

object DomHelpers {

  /**
    * Checks whether element with specified HTML id exists.
    * @param elementId
    * @return
    */
  def existsElementById(elementId: String): Boolean = {
    window.document.getElementById(elementId) != null
  }

  /**
    * Wait for DOM element to exist, then execute the callback.
    * @param elementId
    * @param callback
    */
  def waitForElementByIdAndThenSome(elementId: String, callback: Callback): Unit = {
    def loop(timestamp: Double): Unit = {
      if(existsElementById(elementId))
        callback.runNow()
      else
        window.requestAnimationFrame(loop _)
    }
    loop(0)
  }

  /**
    * Execute the callback after specified time.
    * @param callback
    * @param wait time in ms
    * @return
    */
  def waitAndExecute(callback: Callback, wait: Int = 5): Callback = Callback{
    window.setTimeout(() => callback.runNow(), wait)
  }

}
