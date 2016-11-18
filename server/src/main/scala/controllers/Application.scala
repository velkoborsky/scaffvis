package controllers

import java.io.FileInputStream
import javax.inject.Inject

import components.SampleDatasetComponent
import scaffvis.shared.Api
import scaffvis.shared.model.Molecule
import play.api.Environment
import play.api.mvc._
import resource._
import services.ApiService
import upickle.Js
import upickle.default.{Reader, Writer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Router extends autowire.Server[Js.Value, Reader, Writer]{
  def read[R: Reader](p: Js.Value) = upickle.default.readJs[R](p)
  def write[R: Writer](r: R) = upickle.default.writeJs(r)
}

class Application @Inject() (
                              apiService: ApiService,
                              sampleDatasetComponent: SampleDatasetComponent,
                              implicit val environment: Environment
                            ) extends Controller {

  def index = Action {
    Ok(views.html.index("Scaffold Visualizer"))
  }

  val maxLength: Long = 50 * 1024 * 1024 //50 MB

  def autowireApi(path: String) = Action.async(parse.tolerantText(maxLength = maxLength)) {
    implicit request =>
      val requestParams: Map[String, Js.Value] = upickle.json.read(request.body).asInstanceOf[Js.Obj].value.toMap

      // call Autowire route
      Router.route[Api](apiService)(
        autowire.Core.Request(path.split("/"), requestParams)
      ).map(result => {
        Ok(upickle.json.write(result, 2)).as(JSON)
      })
  }

  def uploadDataset = Action(parse.temporaryFile) {
    implicit request =>
      val file = request.body.file

      //val md = MessageDigest.getInstance("SHA-1");

      val molecules = Try{
        var res: Seq[Molecule] = null
        for {
          is <- managed(new FileInputStream(file))
          //dis <- managed(new DigestInputStream(is, md))
        } {
          res = apiService.loadFromFile(is)
        }
        res
      }

      //print file digest to check upload functionality
      //println(s"File digest: ${md.digest().map("%02X" format _).mkString}")

      molecules match {
        case Success(molecules) => Ok(upickle.default.write(molecules, 2)).as(JSON)
        case Failure(e) => InternalServerError(e.toString)
      }
  }

  def sampleDataset(name: String) = Action {
    sampleDatasetComponent.sampleDataset(name) match {
      case Success(dataset) => Ok(dataset).as(JSON)
      case Failure(e) => InternalServerError(e.toString)
    }
  }

  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
  }
}
