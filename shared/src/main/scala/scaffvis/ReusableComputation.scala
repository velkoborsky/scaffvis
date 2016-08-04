package scaffvis

/**
  * Generic reusable computation which remembers the last value.
  *
  * @param f the function
  * @tparam I input
  * @tparam K key used to compare equality of inputs (simple case: I = K)
  * @tparam O output
  * @param extractKey how to extract K from I
  * @param eq how to compare two K
  */
case class ReusableComputation[I, K, O](f: I => O,
                                        extractKey: I => K,
                                        eq: (K, K) => Boolean
                                       ) extends (I => O) {
  type Input = I
  type Key = K
  type Output = O

  private var cacheKey: K = _
  private var cacheVal: O = _

  override def apply(x: I) = {
    val key = extractKey(x)
    if ((cacheKey == null) || (!eq(cacheKey, key))) { //update
      cacheKey = key
      cacheVal = f(x)
    }
    cacheVal
  }

}

object ReusableComputation {

  def simple[I, O](f: I => O) = simpleE(f, any_==)
  def simpleE[I, O](f: I => O, eq: (I, I) => Boolean) = ReusableComputation[I, I, O](f, identity, eq)

  def anyRefEq[T <: AnyRef](a: T, b: T): Boolean = a eq b
  def any_==[T <: Any](a: T, b: T): Boolean = a == b

  def productMembersEq[P <: Product](p1: P, p2: P): Boolean = {
    if (p1.productArity != p2.productArity)
      return false
    var c: Int = 0
    val cmax = p1.productArity
    while (c < cmax) {
      val p1e = p1.productElement(c)
      val p2e = p2.productElement(c)
      val equal = if (p1e.isInstanceOf[AnyRef] && p2e.isInstanceOf[AnyRef])
        p1e.asInstanceOf[AnyRef] eq p2e.asInstanceOf[AnyRef]
      else
        p1e equals p2e
      if (!equal) return false
      c = c + 1
    }
    true
  }
}
