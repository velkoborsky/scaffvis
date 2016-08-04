package scaffvis.tasks

import java.util.concurrent.atomic.AtomicInteger

import scaffvis.processing.Stopwatch
import scaffvis.stores.ScaffoldHierarchyStore.RawScaffold
import scaffvis.stores.{ProcessingHierarchyStore, ScaffoldHierarchyStore}
import resource._

import scala.collection.JavaConverters._
import scala.collection.mutable

class GenerateHierarchy {
  for {
    processingH <- managed(new ProcessingHierarchyStore(readOnly = true))
    scaffoldH <- managed(new ScaffoldHierarchyStore(readOnly = false))
  } {

    val nextId: () => Int = {
      val counter = new AtomicInteger(1)
      counter.getAndIncrement
    }

    import scaffoldH._

    val stopwatch = Stopwatch()

    println(s"Assigning unique IDs to all scaffolds...")

    //create mappings scaffold <-> id
    for {
      level <- 0 to 8 //the root scaffold does get ID=1
      scaffoldToIdMap = scaffoldH.scaffoldToIdMap(level)

    } {
      for{
        (key, subtreeSize) <- processingH.subtreeSizeMap(level).asScala
      } {
        val id = nextId()
        scaffoldToIdMap.put(key, id)
        idToScaffoldMap.put(id, RawScaffold(level = level.toByte, key = key, subtreeSize = subtreeSize))
      }
      println(s"Done for level $level.")
    }

    println(s"Finished assigning IDs. Total time ${stopwatch.lapTimeMs} ms.")

    println(s"Building parent<->child maps...")

    //create parent/child maps
    val temporaryChildrenMap = mutable.LongMap.empty[mutable.ArrayBuffer[Int]]
    object Lock
    def addChild(parent: Int, child: Int) = Lock.synchronized {
      temporaryChildrenMap.getOrElseUpdate(parent, mutable.ArrayBuffer.empty) += child
    }

    for {
      level <- 1 to 8
      childLevelIdMap = scaffoldH.scaffoldToIdMap(level)
      parentLevelIdMap = scaffoldH.scaffoldToIdMap(level - 1)
    } {
      for {
        (child, parent) <- processingH.parentMap(level).asScala
      }{
        val childId = childLevelIdMap.get(child)
        val parentId = parentLevelIdMap.get(parent)
        parentMap.put(childId, parentId)
        addChild(parentId, childId)
      }
      println(s"Done for levels ${level-1}<->$level.")
    }

    //store the temporary map
    temporaryChildrenMap.foreach({
      case (parent, children) =>
        childrenMap.put(parent.toInt, children.toArray)
    })

    println(s"Finished building parent<->child maps. Total time ${stopwatch.lapTimeMs} ms.")

    println(s"Committing data... (this final step may take a very long time)")
    scaffoldH.commit()
    println(s"Finished committing data. Total time ${stopwatch.lapTimeMs} ms.")

  }
}