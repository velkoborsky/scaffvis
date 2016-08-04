package scaffvis.tasks

import better.files.File
import scaffvis.configuration.Locations

/**
  * A sample task to check the presence of PubChem source files and database files
  */
class CheckPaths {

  println("Checking paths...")

  def exists(file: File): String = if(file.exists) "exists" else "does not exist"
  def checkPath(name: String, path: File): Unit = println(s"$name ${exists(path)} (${path.path})")

  checkPath("PubChem Compound source directory", Locations.pubchemSdfDirectory)
  println("  Use the SCAFFVIS_PUBCHEM_DIR environment variable to use a different path")
  if(Locations.pubchemSdfDirectory.exists)
    println(s"  Found ${Locations.pubchemSdfFiles.size} PubChem sdf.gz files")

  checkPath("Hierarchy store directory", Locations.hierarchyStoreDirectory)
  println("  Use the SCAFFVIS_HIERARCHY_DIR environment variable to use a different path")
  if(Locations.hierarchyStoreDirectory.exists) {
    checkPath("  Scaffold Hierarchy database file", Locations.scaffoldHierarchyStore)
    checkPath("  Processing Hierarchy database file", Locations.processingHierarchyStore)
    checkPath("  PubChem database file", Locations.pubchemStore)
  }

}
