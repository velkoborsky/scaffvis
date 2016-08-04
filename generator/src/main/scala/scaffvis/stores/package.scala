package scaffvis

package object stores {

  /**
    * Scala-arm adaptor for MapDbStore.
    */
  implicit def mapDbStoreResource[A <: MapDbStore] = new resource.Resource[A] {
    override def open(r: A) = r.open()
    override def close(r: A) = r.close()
    override def toString = "Resource[MapDbStore]"
  }

}
