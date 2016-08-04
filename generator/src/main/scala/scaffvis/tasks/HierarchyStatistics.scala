package scaffvis.tasks

import scaffvis.shared.model.{HierarchyLevels, Scaffold}
import scaffvis.stores.ScaffoldHierarchyStore
import scaffvis.stores.ScaffoldHierarchyStore.RawScaffold
import resource._

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.language.implicitConversions
import scala.util.Sorting
import java.util.Locale

import better.files.File
import scaffvis.chemistry.ImagingOps

class HierarchyStatistics {

  val oldLocale = Locale.getDefault
  Locale.setDefault(Locale.US) //temporarily change locale for number formatting

  for {
    scaffoldH <- managed(new ScaffoldHierarchyStore(readOnly = false))
  } {
    import HierarchyStatistics._
    import HierarchyLevels.{bottomLvl, topLvl}

    val rootRaw = scaffoldH.idToScaffoldMap.get(1)
    assert(rootRaw.level == 0)
    val totalMolecules = rootRaw.subtreeSize

    println(s"Based on $totalMolecules compounds")

    /*
    val totalKeyLength = scaffoldH.idToScaffoldMap.values().asScala
      .map(sc => sc.key.length.toLong).sum

    val averageKeyLength = totalKeyLength.toDouble / totalScaffoldCount

    println(s"Average key length: $averageKeyLength")
    */





    val scaffoldsByLevel: HashMap[Byte, Vector[(Int, RawScaffold)]] = HashMap(
      scaffoldH.idToScaffoldMap.asScala.groupBy(_._2.level).mapValues(_.toVector).toSeq:_*
    )

    header("Number of scaffolds per level")
    val scaffoldKeysByLevel = scaffoldsByLevel.mapValues(_.map({ case (id, sc) => id }))

    {
      val levelCounts = scaffoldKeysByLevel.mapValues(_.size)
      val format = "%8d"

      for(l <- topLvl to bottomLvl) {
        println(s"${levelNamePadded(l)} ${format.format(levelCounts(l))}")
      }
      val totalScaffoldCount = scaffoldH.idToScaffoldMap.sizeLong()
      println(s"${padToLevelNameLen("Total:")} ${format.format(totalScaffoldCount)}")
    }



    header("Scaffolds by number of children")
    //precompute number of children for each scaffold
    val childrenCount = HashMap.empty ++ scaffoldH.childrenMap.asScala.mapValues(arr => arr.length)

    val levelChildrenCounts: Map[Byte, Array[Int]] = scaffoldKeysByLevel
      .mapValues{lvlScaffolds =>
        val counts = lvlScaffolds.map(id => childrenCount(id)).toArray
        Sorting.quickSort(counts)
        counts
      }

    println(s"${padToLevelNameLen("Children count:")}               0-100           101-400          401-1600             >1600")
    for(l <- topLvl until bottomLvl) {
      val counts = levelChildrenCounts(l)

      val (under100, over100) = counts.partition(_ <= 100)
      val (from100to400, over400) = over100.partition(_ <= 400)
      val (from400to1600, over1600) = over400.partition(_ <= 1600)

      val topCount = counts.max
      val totalScaffolds = counts.size

      val valuesFormat = "%7s (%8.4f %%)" + "%6d (%8.4f %%)" * 3
      val valuesFormatted = valuesFormat.format(
        under100.size, under100.size.toDouble/totalScaffolds * 100,
        from100to400.size, from100to400.size.toDouble/totalScaffolds * 100,
        from400to1600.size, from400to1600.size.toDouble/totalScaffolds * 100,
        over1600.size, over1600.size.toDouble/totalScaffolds * 100
      )
      println(s"${levelNamePadded(l)} $valuesFormatted")
    }

    header("Number of children percentiles")
    val percentiles = Seq(0.00, 0.01, 0.05, 0.10, 0.25, 0.50, 0.75, 0.90, 0.95, 0.99, 1.00)
    val headerFormat = " %6.2f " * percentiles.length
    val valuesFormat = "%7.0f " * percentiles.length
    println(s"${padToLevelNameLen("Percentile:")} ${headerFormat.format(percentiles.map(_*100):_*)} average")
    for(l <- topLvl until bottomLvl) {
      val sortedCounts = levelChildrenCounts(l)
      val percentileValues = percentiles.map(percentile(sortedCounts))
      val valuesFormatted = valuesFormat.format(percentileValues:_*)
      val avgFormatted = "%7.2f".format(sortedCounts.sum.toDouble / sortedCounts.length)
      println(s"${levelNamePadded(l)} $valuesFormatted $avgFormatted")
    }

    header("Top scaffolds on each level")
      for(l <- topLvl to bottomLvl) {
        println(levelNameUnpadded(l))
        val scaffolds = scaffoldsByLevel(l)

        val bySubtreeSizeDesc = scaffolds.sortWith((a, b) => a._2.subtreeSize > b._2.subtreeSize)

        val top10 = bySubtreeSizeDesc.take(10).toList
          .map{ case(id, RawScaffold(level, key, subtreeSize)) => (id, subtreeSize, key) }

        val top10sum = top10.map{case (_, subtreeSize, _) => subtreeSize}.sum

        def frac(size: Int) = "%6.2f %%".format(size.toDouble/totalMolecules*100)

        val header = Seq(
          "ID",
          "Key",
          "Subtree size",
          "Frac",
          "Number of Children"
        )

        val stringVals = top10.map { case (id, subtreeSize, key) => Seq(
          id.toString,
          key,
          subtreeSize.toString,
          frac(subtreeSize),
          childrenCount.get(id).map(_.toString).getOrElse("n/a")
        )}

        val footer = Seq("", "Total", top10sum.toString, frac(top10sum), "")

        val rows = header :: stringVals ::: footer :: Nil

        val maxLengths = rows.transpose.map(_.map(_.length).max)
        val lineFormat = "  " + maxLengths.map(len => s"%${len}s").mkString(" ")

        val formattedLines = rows.map(cols => lineFormat.format(cols:_*))

        formattedLines.foreach(println)

//        //optionally also generate svg images
//        val ids = top10.map{case (id, _, _) => id}
//        val outDir = File("HierarchyStatisticsOutput")
//        outDir.createIfNotExists(asDirectory = true)
//        ids.map(scaffoldH.get).foreach(scaffoldSvgToFile(outDir))
      }

  }

  Locale.setDefault(oldLocale)
}

object HierarchyStatistics {

  import HierarchyLevels.{bottomLvl, topLvl}
  implicit def intToByte(x: Int): Byte = x.toByte

  /**
    * calculate percentile in a sorted array
    *
    * @param p 0 <= p <= 1
    */
  def percentile(arr: Array[Int])(p: Double): Double = {
    if (p > 1 || p < 0) throw new IllegalArgumentException
    val maxIdx = arr.length - 1
    val fracIdx = maxIdx * p
    val lowerIdx = fracIdx.toInt
    val higherIdx = Math.ceil(fracIdx).toInt
    val lower = arr(lowerIdx)
    val higher = arr(higherIdx)
    val diff = higher - lower
    lower + diff * (fracIdx - lowerIdx)
  }

  { // mini test
    val a1 = Array(1, 2, 5, 7, 8)
    assert(percentile(a1)(0.5) == 5)
    assert(percentile(a1)(0) == 1)
    assert(percentile(a1)(1) == 8)
    val a2 = Array(3, 4, 5, 8)
    assert(percentile(a2)(0.5) == 4.5)
  }

  def header(s: String): Unit = {
    val len = s.length
    println()
    println(s)
    println("-" * len)
  }

  val levelNameUnpadded = (topLvl to bottomLvl).map(l => s"Level $l (${HierarchyLevels.name(l)}):").toVector
  private val levelNameLength = levelNameUnpadded.map(_.length).max
  private val levelNamePaddingFmt = "%1$-" + levelNameLength + "s"
  val levelNamePadded = levelNameUnpadded.map(levelNamePaddingFmt.format(_))
  def padToLevelNameLen(s: String): String = levelNamePaddingFmt.format(s)

  def scaffoldSvgToFile(dir: File)(s: Scaffold): Unit = {
    val width = 400
    val height = 400
    import s._
    val fileName = s"Scaffold-L$level-$id.svg"
    val file = dir / fileName
    val svg = ImagingOps.scaffoldToSvg(s, width = width, height = height, stripSvgTag = false)
    file.write(svg)
  }

}