package annex.concurrency

import org.specs2.mutable.Specification

class Spec3 extends Specification {
  println("Spec3 executing...")

  maybeWaitForOthers()
}
