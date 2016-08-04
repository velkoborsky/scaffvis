package scaffvis.stores

import scaffvis.chemistry.SmilesOps
import scaffvis.hierarchy.ScaffoldHierarchy
import scaffvis.shared.model.{RootScaffold, ScaffoldId}
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class ScaffoldHierarchyTest extends FreeSpec with BeforeAndAfterAll {

  var store = new ScaffoldHierarchyStore(readOnly = false)

  override def beforeAll(): Unit = {
    store.open()
  }

  override def afterAll(): Unit = {
    store.close()
  }

  def hierarchy: ScaffoldHierarchy = store

  "ScaffoldHierarchy" - {

    "parent" - {
      "of Root is None" in {
        assertResult(None)(hierarchy.parent(RootScaffold))
      }
    }

    "findLowestAncestor" - {
      "for C1CC1C1CC1" in {
        val m = SmilesOps.smilesToMolecule("C1CC1C1CC1")
        val la = hierarchy.findLowestScaffold(m)._1
        assert(la > 0)
      }

      "for C1CC1C1CC1C1CC1" in {
        val m = SmilesOps.smilesToMolecule("C1CC1C1CC1C1CC1")
        val la = hierarchy.findLowestScaffold(m)._1
        assert(la > 0)
      }
    }

    "ancestors" - {
      "for ROOT" in {
        val as = hierarchy.scaffoldAncestors(RootScaffold.id)
        assertResult(Array(RootScaffold.id))(as)
      }

      "first level 8 scaffold has 9 ancestors (incl. root)" in {
        def descend(sid: ScaffoldId): List[ScaffoldId] = {
          val children = hierarchy.children(sid)
          val down = if (children.isEmpty) Nil else descend(children.head.id)
          sid :: down
        }
        val descendants = descend(RootScaffold.id)
        assertResult(9)(descendants.length)
        val ancestors = hierarchy.scaffoldAncestors(descendants.last)
        assertResult(9)(ancestors.length)
        assertResult(descendants)(ancestors)

      }
    }

  }

}
