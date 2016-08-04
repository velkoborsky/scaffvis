package scaffvis.client.store.handlers.common

import diode.data.{Pending, Pot, Ready}

import scala.concurrent.duration._

/**
  * Common functionality for Scaffolds.svg and Molecules.svg
  */
object SvgHandlerHelper {

  val retryInterval: Int = 5.second.toMillis.toInt //how long can a request be pending before we try again

  def getIdsToLoad(currentMap: Map[Int, Pot[String]], ids: Seq[Int]): Seq[Int] = {
    def shouldLoad(id: Int): Boolean = currentMap.get(id) match {
      case None => true
      case Some(Ready(_)) => false
      case Some(p: Pending) => p.duration() > retryInterval
      case _ => true
    }
    ids.filter(shouldLoad)
  }

  def updateWithPending(currentMap: Map[Int, Pot[String]], ids: Seq[Int]) = {
    val pending = Pending()
    val updatedEntries = ids.map(id => id -> pending).toMap
    val updatedMap = currentMap ++ updatedEntries
    updatedMap
  }

  def updateWithNewSvgs(currentMap: Map[Int, Pot[String]], updatedIds: Seq[Int], updatedValues: Seq[String]): Map[Int, Pot[String]] = {
    val updatedEntries = updatedIds.zip(updatedValues.map(Ready.apply)).toMap
    val updatedMap = currentMap ++ updatedEntries
    updatedMap
  }

}
