import org.specs2.mutable.Specification

object Test1 extends Specification {
  "Global" should {
    "start at 1" in {
      Global.value.getAndSet(2) must_== 1
    }
  }
}

object Test2 extends Specification {
  "Global" should {
    "start at 1" in {
      Global.value.getAndSet(2) must_== 1
    }
  }
}
