package components

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import resource._
import scaffvis.shared.model._
import services.ApiService
import scala.collection.JavaConverters._

import scala.util.{Failure, Try}

@Singleton
class SampleDatasetComponent @Inject()(
                              lifecycle: ApplicationLifecycle,
                              apiService: ApiService,
                              configuration: Configuration
                            ) {

  private val sampleDatasets: Set[String] =
    configuration.getStringList("scaffvis.sampledatasets")
      .map(_.asScala.toSet)
      .getOrElse(Set.empty)

  private val cache = scala.collection.mutable.Map[String,Try[String]]()

  private def loadSampleDataset(name: String) = {
    Try {
      var res: Seq[Molecule] = null
      for (is <- managed(getClass.getResourceAsStream(s"/sampleDatasets/$name.smiles.gz"))) {
        res = apiService.loadFromFile(is)
      }
      res
    } map {
      molecules => upickle.default.write(molecules, 2)
    }
  }

  def sampleDataset(name: String): Try[String] = {
    if(sampleDatasets.contains(name)) {
      cache.getOrElseUpdate(name, loadSampleDataset(name))
    } else {
      Failure(InvalidDatasetNameException)
    }
  }

}

