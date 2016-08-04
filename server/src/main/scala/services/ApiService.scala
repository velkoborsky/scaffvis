package services

import java.io.{ByteArrayInputStream, InputStream}
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

import scaffvis.chemistry.{ChemMolecule, MoleculeOps, SmilesOps}
import scaffvis.hierarchy.ScaffoldHierarchy
import scaffvis.shared.Api
import scaffvis.shared.model._
import com.google.inject.Singleton
import components.SvgComponent
import play.api.Logger

import scala.collection.mutable
import scala.collection.parallel.immutable.ParVector
import scala.language.implicitConversions

@Singleton
class ApiService @Inject()(
                  scaffoldHierarchy: ScaffoldHierarchy,
                  svgComponent: SvgComponent
                ) extends Api {

  /**
    * Scaffold hierarchy - get children of a scaffold
    */
  override def getChildren(scaffoldId: ScaffoldId): Seq[Scaffold] = {
    scaffoldHierarchy.children(scaffoldId)
  }

  override def getScaffold(scaffoldId: ScaffoldId): Seq[Scaffold] = {
    val ancestors = scaffoldHierarchy.scaffoldAncestors(scaffoldId)
    ancestors.map(scaffoldHierarchy.get)
  }

  override def getMoleculeSvg(smiles: Seq[String]): Seq[String] = {
    smiles.toVector.par
      .map(svgComponent.moleculeToSvg)
      .toVector
  }

  override def getScaffoldSvg(ids: Seq[ScaffoldId]): Seq[String] = {
    ids.toVector.par.map(svgComponent.scaffoldToSvgCached).toVector
  }

  /**
    * Load (and preprocess) dataset
    */
  override def loadFromSmiles(smiles: Seq[String]): Seq[Molecule] = {
    var counter = 0
    def nextId(): Int = {
      counter += 1; counter
    }
    for {
      s <- smiles
      m = SmilesOps.smilesToMolecule(s)
      scaffolds = scaffoldHierarchy.scaffoldAncestors(scaffoldHierarchy.findLowestScaffold(m))
    } yield Molecule(
      id = nextId(),
      smiles = s,
      name = None,
      comment = None,
      scaffolds = scaffolds
    )
  }

  override def loadFromFile(fileContent: Array[Byte]): Seq[Molecule] = {
    val is = new ByteArrayInputStream(fileContent)
    loadFromFile(is)
  }

  val batchSize = 1 << 17

  def loadFromFile(inputStream: InputStream): Seq[Molecule] = {
    val times = mutable.Buffer.empty[Long]
    times += System.currentTimeMillis()

    val moleculeIterator = MoleculeOps.readInputStreamAutodetect(inputStream)

    times += System.currentTimeMillis()

    val counter = new AtomicInteger()
    val batchResults = for {
      batch <- moleculeIterator.grouped(batchSize)
    } yield loadBlockOfMolecules(batch.to[ParVector], counter.incrementAndGet).toVector

    times += System.currentTimeMillis()

    val res = batchResults.toVector.flatten

    times += System.currentTimeMillis()

    val partialTimes = times.sliding(size = 2, step = 1).map(b => b(1) - b(0))
    val totalTime = times.last - times.head
    println(s"time elapsed: $totalTime (${partialTimes.mkString(", ")})")

    res
  }

  private def getMoleculeSmiles(m: ChemMolecule): String = {
    try {
      SmilesOps.moleculeToSmiles(m)
    } catch {
      case e: chemaxon.marvin.io.MolExportException =>
        SmilesOps.moleculeToSmiles(m, chemaxonSmiles = true)
    }
  }

  private def getMoleculeAncestorsDestructive(m: ChemMolecule): Array[ScaffoldId] = {
    try {
      scaffoldHierarchy.findAllScaffoldsAndDestroyMolecule(m)
    } catch {
      case e: RuntimeException =>
        Logger.error("Error calculating scaffolds for a molecule", e)
        Array(RootScaffold.id)
    }
  }

  private def loadBlockOfMolecules(molecules: ParVector[ChemMolecule], nextId: () => Int): ParVector[Molecule] = {

    molecules.foreach(MoleculeOps.convertExplicitHydrogensToImplicit)
    
    val smilesNameComment = molecules.map(m => {
      val smiles = getMoleculeSmiles(m)
      val name = m.getName
      val comment = m.getComment
      (smiles, name, comment)
    })

    val ancestors = molecules.map(getMoleculeAncestorsDestructive)
    //molecules destroyed!

    implicit def stringToOpt(s: String): Option[String] = if(s == null || s.isEmpty) None else Some(s)

    smilesNameComment.zip(ancestors).map{
      case ((smiles, name, comment), scaffolds) => Molecule(
        id = nextId(),
        smiles = smiles,
        name = name,
        comment = comment,
        scaffolds = scaffolds
      )
    }
  }

}
