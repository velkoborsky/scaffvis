package scaffvis.tasks

import better.files.File
import resource._
import scaffvis.processing.Stopwatch
import scaffvis.shared.model.{Scaffold, ScaffoldId}
import scaffvis.stores.ScaffoldHierarchyStore

import scala.collection.JavaConverters._

/**
  * Exports the hierarchy into CSV files
  */
class ExportHierarchy {

  val separator = ","

  val outDir = File("ExportHierarchyOutput")
  outDir.createIfNotExists(asDirectory = true)

  def outputFileName(level: Int) = s"scaffolds_lvl_$level.csv"

  val headerColumns = "Id" :: "Level" :: "Key" :: "SubtreeSize" :: "ParentId" :: "ChildrenIds" :: Nil
  val header = headerColumns.mkString(separator)

  for {
    scaffoldH <- managed(new ScaffoldHierarchyStore(readOnly = true))
  } {

    val stopwatch = Stopwatch()

    for (level <- scaffoldH.topLvl to scaffoldH.bottomLvl) {

      val outFile = outDir/outputFileName(level)
      if(outFile.exists)
        throw new RuntimeException(s"Error: Output file ${outFile.path} already exists!")

      println(s"Exporting hierarchy level $level to file ${outFile.path}")

      val scaffoldsIds = scaffoldH.scaffoldToIdMap(level).values().iterator().asScala

      val lines: Iterator[String] = for (id <- scaffoldsIds) yield {

        val scaffold: Scaffold = scaffoldH.get(id)
        val parentId: Option[ScaffoldId] = scaffoldH.parentId(id)
        val childrenIds: Seq[ScaffoldId] = scaffoldH.childrenIds(id)

        val columns = scaffold.id.toString +: scaffold.level.toString +: scaffold.key +: scaffold.subtreeSize.toString +:
          parentId.map(_.toString).getOrElse("") +: childrenIds.map(_.toString)

        val line = columns.mkString(separator)
        line
      }

      outFile.appendLine(header)
      outFile.appendLines(lines.toSeq:_*)

    }

  }
}