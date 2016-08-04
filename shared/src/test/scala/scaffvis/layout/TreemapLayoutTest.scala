package scaffvis.layout

import scaffvis.layout.TreemapLayout.InputEntry
import org.scalatest.FlatSpec

class TreemapLayoutTest extends FlatSpec {

  val sampleData = {
    val weights = List(6, 6, 4, 3, 2, 2, 1)
    weights.zipWithIndex.map({case (w,i) => InputEntry(i, w)})
  }

  "Treemap.layout" should "return something" in {
    val layout = TreemapLayout.layout(sampleData)
    assert(layout.nonEmpty)
  }

  "Treemap.layout" should "return something too" in  {
    val layout = TreemapLayout.layout(sampleData, Rect(0, 0, 1, 1.5))
    assert(layout.nonEmpty)
  }
}