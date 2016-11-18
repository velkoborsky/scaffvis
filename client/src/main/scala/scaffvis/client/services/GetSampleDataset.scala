package scaffvis.client.services

import org.scalajs.dom
import scaffvis.shared.model.Molecule

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GetSampleDataset {
  def apply(name: String): Future[Seq[Molecule]] = {
    dom.ext.Ajax.get(
      url = s"/get/sampleDataset/$name"
    ).map(r => upickle.default.read[Seq[Molecule]](r.responseText))
  }
}
