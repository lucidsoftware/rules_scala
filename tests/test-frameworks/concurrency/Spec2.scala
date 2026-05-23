package annex.concurrency

import org.specs2.mutable.Specification

class Spec2 extends Specification {
  println("Spec2 executing...")

  maybeWaitForOthers()
}
