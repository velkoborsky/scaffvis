package scaffvis.client.store.actions

import scaffvis.shared.model.{Molecule, MoleculeId}
import diode.Action
import org.scalajs.dom.File

object MoleculesActions {

  case class ActivateMolecule(molecule: MoleculeId) extends Action
  case object DeactivateMolecule extends Action
  case class SelectMolecule(molecule: MoleculeId) extends Action
  case class SelectMolecules(molecule: Iterable[MoleculeId]) extends Action
  case class DeselectMolecule(molecule: MoleculeId) extends Action
  case class DeselectMolecules(molecule: Iterable[MoleculeId]) extends Action
  case object DeselectAllMolecules extends Action

  case class LoadMoleculeSvg(molecules: Seq[MoleculeId]) extends Action
  case class UpdateMoleculeSvg(molecules: Seq[MoleculeId], svg: Seq[String]) extends Action

  case class ReplaceMolecules(molecules: Seq[Molecule]) extends Action
  case class LoadMoleculesFailed(exception: Throwable) extends Action

//  case class LoadMoleculesFromSmiles(smiles: Seq[String]) extends Action
//  case class LoadMoleculesFromFile(fileContent: Array[Byte]) extends Action
  case class LoadMoleculesFromJsFile(file: File) extends Action
  case class LoadMoleculesLocally(file: File) extends Action
  case class LoadMoleculesFromSampleDataset(name: String) extends Action

}
