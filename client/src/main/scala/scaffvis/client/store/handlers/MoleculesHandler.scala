package scaffvis.client.store.handlers

import autowire._
import diode.data.{Failed, Pending, Pot, Ready}
import diode.{ActionHandler, ActionResult, Effect, ModelRW}
import scaffvis.client.services._
import scaffvis.client.store.actions.MoleculesActions._
import scaffvis.client.store.handlers.common.SvgHandlerHelper
import scaffvis.client.store.model.{IndexedMolecules, Molecules}
import scaffvis.client.store.serializer.BooPickleSerializer
import scaffvis.client.util.FileIO
import scaffvis.shared.Api

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

class MoleculesHandler[M](model: ModelRW[M, Pot[Molecules]]) extends ActionHandler(model) {

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case SelectMolecule(moleculeId) =>
      updated(value.map(ms => ms.copy(selected = ms.selected + moleculeId)))
    case DeselectMolecule(moleculeId) =>
      updated(value.map(ms => ms.copy(selected = ms.selected - moleculeId)))
    case SelectMolecules(moleculeIds) =>
      updated(value.map(ms => ms.copy(selected = ms.selected ++ moleculeIds)))
    case DeselectMolecules(moleculeIds) =>
      updated(value.map(ms => ms.copy(selected = ms.selected -- moleculeIds)))
    case DeselectAllMolecules =>
      updated(value.map(ms => ms.copy(selected = ms.selected.empty)))
    case ActivateMolecule(moleculeId) =>
      updated(value.map(ms => ms.copy(active = Some(moleculeId))))
    case DeactivateMolecule =>
      updated(value.map(ms => ms.copy(active = None)))

    case LoadMoleculeSvg(moleculeIds) =>
      value match {
        case Ready(molecules) =>
          val currentMap = molecules.svg
          val idsToLoad = SvgHandlerHelper.getIdsToLoad(currentMap, moleculeIds)
          if(idsToLoad.isEmpty) noChange
          else {
            val smiles = idsToLoad.flatMap(id => molecules.get(id).map(_.smiles))
            updated(
              newValue = value.map(_.copy(svg = SvgHandlerHelper.updateWithPending(currentMap, idsToLoad))),
              effect = Effect(AutowireClient[Api].getMoleculeSvg(smiles).call().map(svg => UpdateMoleculeSvg(idsToLoad, svg))
              )
            )
          }
        case _ => noChange
      }
    case UpdateMoleculeSvg(moleculeIds, svgs) =>
      updated(value.map(ms => ms.copy(svg = SvgHandlerHelper.updateWithNewSvgs(ms.svg, moleculeIds, svgs))))

    case ReplaceMolecules(molecules) =>
      updated(Ready(Molecules(IndexedMolecules(molecules.toVector))))

    case LoadMoleculesFailed(exception) =>
      updated(Failed(exception))

//    case LoadMoleculesFromSmiles(smiles) =>
//      updated(
//        newValue = Pending(),
//        effect = Effect(AutowireClient[Api].loadFromSmiles(smiles).call().map(ReplaceMolecules))
//      )
//    case LoadMoleculesFromFile(fileContent) =>
//      updated(
//        newValue = Pending(),
//        effect = Effect(AutowireClient[Api].loadFromFile(fileContent).call().map(ReplaceMolecules))
//      )
    case LoadMoleculesFromJsFile(file) =>
      updated(
        newValue = Pending(),
        effect = Effect(UploadDataset(file).map(ReplaceMolecules)
          .recover({ case e: Throwable => LoadMoleculesFailed(e)})
        )
      )
    case LoadMoleculesLocally(file) =>
      updated(
        newValue = Pending(),
        effect = Effect(FileIO.fileToByteArray(file).map(BooPickleSerializer.load))
      )
  }

}
