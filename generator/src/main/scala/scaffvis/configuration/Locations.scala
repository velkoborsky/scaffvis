package scaffvis.configuration

import better.files.File

/**
  * Define default database files locations and extract them from the environment variables.
  */
object Locations {

  /**
    * The location of PubChem files to be imported.
    */
  lazy val pubchemSdfDirectory = {
    val dirPath: String = sys.env.getOrElse("SCAFFVIS_PUBCHEM_DIR", "pubchem")
    val dir = File(dirPath)
    if(!dir.exists)
      println(s"Pubchem source directory ${dir.pathAsString} does not exist")
    dir
  }

  lazy val pubchemSdfFiles = pubchemSdfDirectory.glob("**\\\\*.sdf.gz").toVector

  /**
    * Directory containing the internal database files.
    */
  lazy val hierarchyStoreDirectory = {
    val dirPath: String = sys.env.getOrElse("SCAFFVIS_HIERARCHY_DIR", "hierarchy")
    val dir = File(dirPath)
    if(!dir.exists)
      println(s"Hierarchy store directory ${dir.pathAsString} does not exist")
    dir
  }

  lazy val pubchemStore = hierarchyStoreDirectory / "pubchem.mapdb"
  lazy val scaffoldHierarchyStore = hierarchyStoreDirectory / "scaffoldHierarchy.mapdb"
  lazy val processingHierarchyStore = hierarchyStoreDirectory / "processingHierarchy.mapdb"

}
