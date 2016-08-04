package scaffvis.shared.model

/**
  * Defines number and names of levels in the used chemical hierarchy.
  *
  * Chemical definition is in the ScaffoldTransformation object in the generator project.
  */
object HierarchyLevels {

  val topLvl = 0 //do not change this - zero based level indexing is needed for easy storage in arrays
  val bottomLvl = 8 //change this to increase or decrease the number of levels

  val count = bottomLvl - topLvl + 1

  /**
    * Define the name of each hierarchy level.
    */
  private val names: Array[String] = {
    val a = Array.ofDim[String](count)
    a(0) = "Root"
    a(1) = "Ring Count"
    a(2) = "Ring Connectivity"
    a(3) = "Ring Connectivity Extended"
    a(4) = "Oprea"
    a(5) = "Murcko Rings"
    a(6) = "Murcko Rings with Linkers"
    a(7) = "Rings with Linkers"
    a(8) = "Rings with Linkers Stereo"
    a
  }

  /**
    * Provide safe access to the level names from the code. Currently only used in the ScaffoldTransformation class.
    */
  val root = 0
  val ringCount = 1
  val ringConnectivity = 2
  val ringConnectivityExtended = 3
  val oprea = 4
  val murckoRings = 5
  val murckoRingsWithLinkers = 6
  val ringsWithLinkers = 7
  val ringsWithLinkersStereo = 8

  /**
    * Retuns the name of a given level.
    */
  def name(level: Int): String = names(level)

}
