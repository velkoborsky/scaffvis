package scaffvis.client.services

import scaffvis.shared.model.Molecule
import org.scalajs.dom
import org.scalajs.dom.File

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UploadDataset {
  def apply(dataset: File): Future[Seq[Molecule]] = {
    dom.ext.Ajax.post(
      url = "/upload/dataset",
      data = dataset
    ).map(r => upickle.default.read[Seq[Molecule]](r.responseText))
  }
}
