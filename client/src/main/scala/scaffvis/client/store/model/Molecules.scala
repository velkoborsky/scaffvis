package scaffvis.client.store.model

import scaffvis.shared.model._
import diode.data.Pot

import scala.collection.immutable.{HashMap, HashSet}

case class Molecules(im: IndexedMolecules,
                     svg: Map[MoleculeId, Pot[String]] = Map.empty,
                     selected: Set[MoleculeId] = Set.empty, active: Option[MoleculeId] = None) {

  def molecules: Iterable[Molecule] = im.molecules
  def scaffoldMolecules(scaffoldId: ScaffoldId) = im.scaffoldIndex.getOrElse(scaffoldId, Set.empty)
  def get(moleculeId: MoleculeId): Option[Molecule] = im.moleculeMap.get(moleculeId)
  def get_!(moleculeId: MoleculeId): Molecule = im.moleculeMap.apply(moleculeId)
}

case class IndexedMolecules(molecules: Vector[Molecule]) {

  val moleculeMap: Map[MoleculeId, Molecule] = HashMap(molecules.map(m => m.id -> m).toSeq:_*)

  val scaffoldIndex: Map[ScaffoldId,Set[MoleculeId]] = {
    val pairs: Iterable[(ScaffoldId, MoleculeId)] = for {
      m <- molecules
      s <- m.scaffolds
    } yield s -> m.id
    val groups: Map[ScaffoldId, Iterable[(ScaffoldId, MoleculeId)]] = pairs.groupBy(_._1)
    val index: Map[ScaffoldId, Set[MoleculeId]] = groups.mapValues(i => i.map(_._2).to[HashSet])
    HashMap(index.toSeq:_*)
  }

}