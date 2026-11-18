import org.specs2.mutable.Specification

object Test3 extends Specification {
  "-Dexample" should {
    "not already be set by another test class" in {
      Option(System.setProperty("example", "value")) must beNone
    }
  }
}

object Test4 extends Specification {
  "-Dexample" should {
    "not already be set by another test class" in {
      Option(System.setProperty("example", "value")) must beNone
    }
  }
}
