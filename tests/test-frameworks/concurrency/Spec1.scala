package annex.concurrency

import org.specs2.mutable.Specification

class Spec1 extends Specification {
  println("Spec1 executing...")

  maybeWaitForOthers()
}
