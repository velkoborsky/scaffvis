package scaffvis.client.store.model

import scaffvis._
import scaffvis.shared.model._
import diode.data.{Empty, Pending, Pot, Ready}

import scala.annotation.tailrec
import scala.collection.immutable.HashMap


trait ScaffoldHierarchy {
  def apply(scaffoldId: ScaffoldId): Scaffold

  def get(scaffoldId: ScaffoldId): Option[Scaffold]

  def children(parentId: ScaffoldId): Pot[Seq[Scaffold]]
  def children(parent: Scaffold): Pot[Seq[Scaffold]] = children(parent.id)

  def parent(childId: ScaffoldId): Option[Scaffold]
  def parent(child: Scaffold): Option[Scaffold] = parent(child.id)

  def path(scaffold: Scaffold): IndexedSeq[Scaffold] = {
    @tailrec
    def loop(self: Scaffold, acc: List[Scaffold]): List[Scaffold] = {
      val upToSelf = self :: acc
      parent(self) match {
        case Some(p) => loop(p, upToSelf)
        case None => upToSelf
      }
    }
    loop(scaffold, Nil).toVector
  }

  def root: Scaffold = RootScaffold

  def storeChildren(parentId: ScaffoldId, children: Pot[Seq[Scaffold]]): ScaffoldHierarchy

  //stores scaffolds in order - the parent of the first scaffold must be in hierarchy
  def storeScaffold(chain: Seq[Scaffold]): ScaffoldHierarchy

  //def updated(parentId: ScaffoldId, children: Seq[Scaffold]): ScaffoldTree = updated(parentId, Ready(children))

}

object ScaffoldHierarchy {

  def apply(): ScaffoldHierarchy = empty

  val empty: ScaffoldHierarchy = ScaffoldHierarchyImpl(
    elementsMap = HashMap(RootScaffold.id -> RootScaffold),
    childrenMap = HashMap.empty,
    parentMap = HashMap.empty
  )

  private case class ScaffoldHierarchyImpl(
                                       elementsMap: Map[ScaffoldId, Scaffold],
                                       childrenMap: Map[ScaffoldId, Pot[Seq[Scaffold]]],
                                       parentMap: Map[ScaffoldId, Scaffold]
                                     ) extends ScaffoldHierarchy {


    override def apply(scaffoldId: ScaffoldId): Scaffold =
      elementsMap(scaffoldId)


    override def get(scaffoldId: ScaffoldId): Option[Scaffold] =
      elementsMap.get(scaffoldId)


    override def children(parentId: ScaffoldId): Pot[Seq[Scaffold]] = {
      childrenMap.getOrElse(parentId, Pot.empty)
    }

    override def parent(childId: ScaffoldId): Option[Scaffold] = {
      parentMap.get(childId)
    }

    override def storeChildren(parentId: ScaffoldId, childrenPot: Pot[Seq[Scaffold]]): ScaffoldHierarchy = {
      val parentScaffold = elementsMap(parentId)

      childrenPot match {

        case Ready(children) =>
          val elementsMapN = elementsMap ++ (children.map(child => (child.id -> child)))
          val parentMapN = parentMap ++ (children.map(child => (child.id -> parentScaffold)))
          val childrenMapN = childrenMap + (parentId -> childrenPot)
          ScaffoldHierarchyImpl(elementsMap = elementsMapN, childrenMap = childrenMapN, parentMap = parentMapN)

        case Pending(_) =>
          val currentChildren = childrenMap.getOrElse(parentId, Empty)
          currentChildren match {
            case Ready(_) =>
              this //do not update Ready with Pending
            case _ =>
              val childrenMapN = childrenMap + (parentId -> childrenPot)
              ScaffoldHierarchyImpl(elementsMap = elementsMap, childrenMap = childrenMapN, parentMap = parentMap)
          }

        case _ => ?!!
      }
    }

    //stores a chain of scaffolds from Root to a scaffold
    override def storeScaffold(chain: Seq[Scaffold]): ScaffoldHierarchy = {

      @tailrec
      def add(parent: Scaffold, descendants: Seq[Scaffold], em: Map[ScaffoldId, Scaffold], pm: Map[ScaffoldId, Scaffold]
             ): (Map[ScaffoldId, Scaffold], Map[ScaffoldId, Scaffold]) = {
        descendants match {
          case Nil => (em, pm)
          case current +: tail =>
            val nem =
              if (em.contains(current.id)) em
              else em + (current.id -> current)
            val npm = pm + (current.id -> parent)
            add(current, tail, nem, npm)
        }
      }
      chain match {
        case RootScaffold +: tail => //OK
          val (elementsMapN, parentMapN) = add(RootScaffold, tail, elementsMap, parentMap)
          ScaffoldHierarchyImpl(elementsMap = elementsMapN, childrenMap = childrenMap, parentMap = parentMapN)
        case s +: _ =>
          println(s"The chain is expected to start with the RootScaffold, got #${s.id} instead!")
          ?!!
        case _ =>
          println(s"A Seq is expected!")
          ?!!
      }
    }
  }

}