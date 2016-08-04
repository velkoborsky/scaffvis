package components

import javax.inject.{Inject, Singleton}

import scaffvis.chemistry.{ChemMolecule, ImagingOps, SmilesOps}
import scaffvis.hierarchy.ScaffoldHierarchy
import scaffvis.shared.model._
import org.mapdb.{DBMaker, HTreeMap, Serializer}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class SvgComponent @Inject()(
                              lifecycle: ApplicationLifecycle,
                              scaffoldHierarchy: ScaffoldHierarchy,
                              configuration: Configuration
                            ) {

  val cacheSize: Long = configuration.getBytes("scaffvis.svgcomponent.cachesize").getOrElse(32 * 1024 * 1024) //32 MB
  val cacheDb = DBMaker.memoryDB().transactionDisable().cacheLRUEnable().compressionEnable().make()
  val cache: HTreeMap[Int, String] = cacheDb.hashMapCreate("cache")
    .expireMaxSize(cacheSize)
    .keySerializer(Serializer.INTEGER)
    .valueSerializer(Serializer.STRING_ASCII)
    .make()

  lifecycle.addStopHook { () =>
    Future.successful(cacheDb.close())
  }

  /**
    * width of generated images
    */
  val width = 400
  /**
    * height of generated images
    */
  val height = 400

  /**
    * SVG representation of a scaffold.
    *
    * @param scaffoldId
    * @return inner html of an SVG image, the <svg> tag is not included
    */
  def scaffoldToSvgCached(scaffoldId: ScaffoldId) = {
    if (cache.containsKey(scaffoldId)) {
      cache.get(scaffoldId)
    } else {
      val scaffold = scaffoldHierarchy.get(scaffoldId)
      val svg = ImagingOps.scaffoldToSvg(scaffold, width, height, stripSvgTag = true)
      cache.put(scaffoldId, svg)
      svg
    }
  }

  /**
    * SVG representation of a molecule
    * @param smiles
    * @return inner html of an SVG image, the <svg> tag is not included
    */
  def moleculeToSvg(smiles: String) = {
    val molecule = SmilesOps.smilesToMolecule(smiles)
    ImagingOps.smilesToSvg(molecule, width, height, stripSvgTag = true)
  }

}
