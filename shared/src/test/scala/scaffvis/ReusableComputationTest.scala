package scaffvis


import java.util.concurrent.atomic.AtomicInteger

import scaffvis.ReusableComputation._
import org.scalatest.FreeSpec

class ReusableComputationTest extends FreeSpec {

  "Returns results" in {
    type I = Int
    type K = Int
    type O = Int
    type RC = ReusableComputation[I, K, O]

    lazy val double: RC = ReusableComputation(2 * _, identity, any_==)

    assertResult(4)(double(2))
    assertResult(4)(double(2))
    assertResult(18)(double(9))
  }

  "Runs calculation only once for primitive input" in {
    type I = Int
    type K = Int
    type O = Int
    type RC = ReusableComputation[I, K, O]

    val counter = new AtomicInteger(0)

    def sideEffectingDouble(i: Int) = {
      counter.incrementAndGet()
      2 * i
    }

    lazy val double: RC = ReusableComputation(sideEffectingDouble, identity, any_==)

    assertResult(6)(double(3))
    assertResult(1)(counter.get())
    assertResult(6)(double(3))
    assertResult(1)(counter.get())
  }

  "Reruns calculation for compK false" in {
    type I = Int
    type K = Int
    type O = Int
    type RC = ReusableComputation[I, K, O]

    val counter = new AtomicInteger(0)
    def getCount(i: Int) = counter.incrementAndGet()

    lazy val f: RC = ReusableComputation(getCount, identity, (_, _) => false)

    assertResult(1)(f(3))
    assertResult(2)(f(3))
  }

  "Caches calculation with reference equality" in {
    class C(val i: Int) {
      override def equals(that: Any): Boolean = false
    }

    val c = new C(1)
    assert(c eq c)
    assert(c != c)

    val counter = new AtomicInteger(0)

    def sideEffectingDouble(c: C) = {
      counter.incrementAndGet()
      2 * c.i
    }

    type I = C
    type K = C
    type O = Int
    type RC = ReusableComputation[I, K, O]
    lazy val double: RC = ReusableComputation(sideEffectingDouble, identity, anyRefEq)

    assertResult(2)(double(c))
    assertResult(1)(counter.get())
    assertResult(2)(double(c))
    assertResult(1)(counter.get())

  }

  "Caches based on extracted key" in {
    type I = (Int, Int)
    type K = Int
    type O = Int
    type RC = ReusableComputation[I, K, O]

    lazy val f: RC = ReusableComputation({ case (i, j) => i + j }, { case (i, j) => i }, any_==)

    assertResult(5)(f((2, 3)))
    assertResult(5)(f((2, 7)))
    assertResult(10)(f((3, 7)))
    assertResult(9)(f((2, 7)))
  }

  "Simple case for I=K" in {
    type I = Int
    type O = Int

    lazy val f = ReusableComputation[I, I, O](i => 3 * i, identity, any_==)

    assertResult(6)(f(2))
  }

}