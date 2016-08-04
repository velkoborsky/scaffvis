package scaffvis.tasks

import scaffvis.hierarchy.{ScaffoldKeyAndMolecule, HierarchyTransformations}
import scaffvis.processing.{ProcessingController, ProcessingHelper, Stopwatch}
import scaffvis.shared.model.HierarchyLevels
import ProcessingHelper._
import scaffvis.chemistry.SmilesOps
import scaffvis.stores.{ProcessingHierarchyStore, PubchemStore}
import resource._

import scala.concurrent.duration._

class GenerateScaffolds {
  import GenerateScaffolds._

  for {
    processingH <- managed(new ProcessingHierarchyStore(readOnly = false))
  } {

    for {
      pubchem <- managed(new PubchemStore)
    } {

      val stopwatch = Stopwatch()
      println(s"Processing native Pubchem compounds...")

      val parentMapper: (String) => String =
        (smiles) => {
          val molecule = SmilesOps.smilesToMolecule(smiles)
          HierarchyTransformations.fromNative(molecule, clone = false).key
        }
      process[Long, String, String, Int](
        source = pubchem.compounds,
        mapper = (compoundId: Long, compoundSmiles: String) => (parentMapper(compoundSmiles), 1),
        adder = createAdderCounter(processingH.subtreeSizeMap(HierarchyLevels.ringsWithLinkersStereo)),
        controller = ProcessingController.withLongKey(processingH, "processPubchem"),
        errorLogger = createErrorLogger(processingH.pubchemProcessingErrorMap)
      )

      println(s"Finished processing Pubchem compounds. " +
        s"Total time: ${stopwatch.totalTimeMs} ms. " +
        s"Errors: ${processingH.pubchemProcessingErrorMap.size()}.")
    }

    def processLevel(level: Int): Unit = {

      val stopwatch = Stopwatch()
      println(s"Processing level ${level}: ${HierarchyLevels.name(level)}...")

      val parentMapper: (String) => String = wrap(HierarchyTransformations.fromLevel(level))
      val parentSmilesAdder = createAdderReplace(processingH.parentMap(level)) //save parent for the same level
      val parentSubtreeSizeAdder = createAdderCounter(processingH.subtreeSizeMap(level-1)) //count subtree size for the next level

      process[String, Int, String, (Int, String)](
        source = processingH.subtreeSizeMap(level),
        mapper = (smiles, subtreeSize) => {
          val parent = parentMapper(smiles)
          (smiles, (subtreeSize, parent))
        },
        adder = {case (smiles, (subtreeSize, parent)) =>
          parentSmilesAdder(smiles, parent)
          parentSubtreeSizeAdder(parent, subtreeSize)
        },
        controller = ProcessingController.withStringKey(processingH, s"processLevel_$level"),
        errorLogger = createErrorLogger(processingH.processingErrorMap(level)),
        timeOut = if (level <= HierarchyLevels.oprea) 10.seconds else 1.second
      )

      println(s"Finished processing level ${level}. " +
        s"Total time: ${stopwatch.totalTimeMs} ms. " +
        s"Errors: ${processingH.processingErrorMap(level).size()}.")
    }

    for {
      level <- HierarchyLevels.bottomLvl until HierarchyLevels.topLvl by -1
    } {
      processLevel(level)
    }
  }

}

object GenerateScaffolds {

  def wrap(transformation: ScaffoldKeyAndMolecule => ScaffoldKeyAndMolecule): (String) => String = {
    (smiles) => transformation(ScaffoldKeyAndMolecule(smiles)).key
  }
}
