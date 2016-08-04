package scaffvis.client.services

import org.scalajs.dom
import upickle.Js
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AutowireClient extends autowire.Client[Js.Value, Reader, Writer] {
  override def doCall(req: Request): Future[Js.Value] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.json.write(Js.Obj(req.args.toSeq:_*)),
      headers = Map("Content-Type" -> "application/json;charset=UTF-8")
    ).map(r => upickle.json.read(r.responseText))
  }

  override def read[Result: Reader](p: Js.Value) = readJs[Result](p)
  override def write[Result: Writer](r: Result) = writeJs(r)
}
