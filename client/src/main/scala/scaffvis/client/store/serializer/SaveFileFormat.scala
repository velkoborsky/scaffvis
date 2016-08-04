package scaffvis.client.store.serializer

import scaffvis.shared.model._

/**
  * Internal save file format common for all serializers, handled by Serializer trait
  */
case class SaveFileFormat(
                           molecules: Array[Molecule],
                           selected: Option[Array[MoleculeId]]
                           //svg??
                           ) {
  override def toString: String =
    s"SaveFileFormat(molecules: ${molecules.length})"
}
