package scaffvis.chemistry

import java.io.InputStream

import chemaxon.calculations.hydrogenize.Hydrogenize
import chemaxon.formats.MolImporter
import chemaxon.marvin.io.MPropHandler
import chemaxon.struc.MPropertyContainer

import scala.collection.JavaConverters._


/**
  * Provides a few operations on molecules and methods from reading molecules from files.
  */
object MoleculeOps {

  def readInputStreamSdfGz(is: InputStream): Iterator[ChemMolecule] =
    new MolImporter(is, "gzip:sdf").iterator().asScala

  def readInputStreamAutodetect(is: InputStream): Iterator[ChemMolecule] =
    new MolImporter(is).iterator().asScala

  def convertExplicitHydrogensToImplicit(molecule: ChemMolecule) = Hydrogenize.convertExplicitHToImplicit(molecule)

  def getProperties(molecule: ChemMolecule): MoleculeProperties = new MoleculeProperties(molecule.properties())

  class MoleculeProperties private[chemistry] (propertyContainer: MPropertyContainer) {

    def apply(key: String) = MPropHandler.convertToString(propertyContainer, key)

  }

}
