package scaffvis.processing

import java.util.concurrent.ConcurrentNavigableMap
import java.util.concurrent.atomic.AtomicInteger

import org.mapdb.BTreeMap

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._

object ProcessingHelper {

  val batchSize = 1 << 19

  /*
  process source, commit in batches
  map each entry using mapper
  then save it using adder
   */
  /**
    * Processes the source. Maps values using the mapper. Adds them into a target using the adder. Commits in batches
    * and resumes failed compuatation using the controller.
    *
    * @param source Source of entries to be processed.
    * @param mapper Maps a source entry into a target entry.
    * @param adder Adds/saves the target entry into a target.
    * @param controller Provides the starting point and commit functionality.
    * @param errorLogger Logs a mapper errror
    * @tparam SK Source key type.
    * @tparam SV Source value type.
    * @tparam TK Target key type.
    * @tparam TV Target value type.
    */
  def process[SK, SV, TK, TV](source: ConcurrentNavigableMap[SK, SV],
                              mapper: (SK, SV) => (TK, TV),
                              adder: (TK, TV) => Unit,
                              controller: ProcessingController[SK],
                              errorLogger: (SK, String) => Unit,
                              timeOut: Duration = 1.second
                             ): Unit = {


    val stopwatch = Stopwatch()
    val batchCounter = new AtomicInteger(0)
    val sourceTotalBatchCount = Math.ceil(source.size().toDouble / batchSize).toInt

    val (subSource, subsourceBatchCount) = controller.lastProcessedKey match {
      case None => {
        (source, sourceTotalBatchCount.toString)
      }
      case Some(key) => {
        println(s"Resuming from key >$key")
        (source.tailMap(key, false), s"?<=$sourceTotalBatchCount")
      }
    }

    val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null) //another global-like EC

    for (batch <- subSource.entrySet().iterator().asScala.grouped(batchSize)) {
      val b = batch.toParArray
      b.foreach { e =>
        val sourceKey = e.getKey
        val sourceValue = e.getValue
        try {
          val mapped = Future(mapper(sourceKey, sourceValue))(ec)
          val (targetKey, targetValue) = Await.result(mapped, timeOut)
          adder(targetKey, targetValue)
        } catch {
          case te: TimeoutException =>
            println(s"Timeout for $sourceKey")
            errorLogger(sourceKey, e.toString)
          case e: Exception => errorLogger(sourceKey, e.toString)
        }
      }

      controller.commit(b.last.getKey) //last key
      val finishedBatch = batchCounter.incrementAndGet()
      println(s"Finished batch $finishedBatch/$subsourceBatchCount in ${stopwatch.lapTimeMs} ms.")
    }
  }

  def createAdderCounter[KeyType](map: BTreeMap[KeyType, Int]): (KeyType, Int) => Unit = {
    object Lock
    (key, value) => Lock.synchronized {
      val count = map.getOrDefault(key, 0)
      map.put(key, count + value)
    }
  }

  def createAdderReplace[KeyType, ValueType](map: BTreeMap[KeyType, ValueType]): (KeyType, ValueType) => Unit = {
    (key, value) => {
      map.put(key, value)
    }
  }

  def createAdderSingle[KeyType, ValueType](map: BTreeMap[KeyType, ValueType]): (KeyType, ValueType) => Unit = {
    (key, value) => {
      if(map.containsKey(key)) {
        throw new IllegalArgumentException(s"Key $key already exists.")
      }
      map.put(key, value)
    }
  }

//  def createAdderStringArr[KeyType](map: BTreeMap[KeyType, Array[String]]): (KeyType, String) => Unit = {
//    (key, value) => {
//      if(map.containsKey(key)) {
//        val values: Array[String] = map.get(key)
//        map.put(key, values :+ value)
//      } else {
//        map.put(key, Array(value))
//      }
//    }
//  }

  def createErrorLogger[KeyType](map: BTreeMap[KeyType, String]): (KeyType, String) => Unit = {
    (key, msg) => map.put(key, msg)
  }

}
