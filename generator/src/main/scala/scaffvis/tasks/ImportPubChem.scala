package scaffvis.tasks

import java.util.concurrent.atomic.AtomicInteger

import better.files.File
import scaffvis.chemistry.{ChemMolecule, MoleculeOps, SmilesOps}
import scaffvis.configuration.Locations
import scaffvis.stores.PubchemStore
import resource._

import scala.collection.JavaConverters._

class ImportPubChem {

  for {
    pubchem <- managed(new PubchemStore(readOnly = false))
  } {

    def isAlreadyProcessed(f: File): Boolean = {
      if (pubchem.files.contains(f.name)) {
        println(s"Skipping ${f.name} - already processed")
        true
      } else false
    }

    val filesToProcess = Locations.pubchemSdfFiles.filterNot(isAlreadyProcessed)

    val totalCount = filesToProcess.size
    var done = new AtomicInteger(0)

    println(s"Importing ${totalCount} Pubchem SDF files")

    val onFileProcessingStart: String => Unit = name => {
      println(s"Processing $name")
    }

    val onFileProcessed: String => Unit = name => {
      pubchem.files.add(name)
      pubchem.commit()
      val d = done.incrementAndGet()
      println(s"Finished importing $name (${done}/${totalCount})")
    }

    val importMolecule: (ChemMolecule) => Unit = m => {
      /* for importing non-PubChem data, see the ApiService class in the server project */
      val moleculeProperties = MoleculeOps.getProperties(m)
      val pubchemCompoundId = moleculeProperties("PUBCHEM_COMPOUND_CID").toLong
      val openEyeIsoSmiles = moleculeProperties("PUBCHEM_OPENEYE_ISO_SMILES")
      val smiles = SmilesOps.canonicalize(openEyeIsoSmiles)
      pubchem.putCompound(pubchemCompoundId, smiles)
    }

    for {
      file <- filesToProcess.par
      managedInputStream <- file.inputStream
    } {
      onFileProcessingStart(file.name)
      val moleculeIterator = MoleculeOps.readInputStreamSdfGz(managedInputStream)
      moleculeIterator.foreach(importMolecule)
      onFileProcessed(file.name)
    }

  }

}
