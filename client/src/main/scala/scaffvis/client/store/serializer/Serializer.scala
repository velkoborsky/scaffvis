package scaffvis.client.store.serializer

import scaffvis.client.store.Store
import scaffvis.client.store.actions.MoleculesActions
import scaffvis.client.store.model.Model
import diode.{Action, ActionBatch, NoAction}

/**
  * Abstract serializer class, handles Store updates and reading,
  * leaves serialization and deserialization to implementations
  *
  * @tparam T type of serialized data
  */
trait Serializer[T] {

  def save(): T = save(modelRW.value)

  def save(model: Model, saveSelection: Boolean = true): T = serialize(extractSaveFileFormat(model, saveSelection))

  //returns Seq of actions that restore the dataset
  def load(t: T): Action = updateStore(deserialize(t))

  protected def serialize(data: SaveFileFormat): T

  protected def deserialize(t: T): SaveFileFormat

  protected val modelRW = Store.zoomRW(identity)((_, model) => model)

  protected def extractSaveFileFormat(model: Model, selection: Boolean): SaveFileFormat = {
    val ms = model.molecules.getOrElse(throw new RuntimeException("No dataset loaded"))
    val molecules = ms.im.molecules.toArray
    val selected = if(selection) Some(ms.selected.toArray) else None
    SaveFileFormat(molecules, selected)
  }

  protected def updateStore(data: SaveFileFormat): Action = {
    val updateMolecules = MoleculesActions.ReplaceMolecules(data.molecules)
    val restoreSelection = data.selected.map(s => MoleculesActions.SelectMolecules(s)).getOrElse(NoAction)
    ActionBatch(updateMolecules, restoreSelection)
  }

}
