import org.specs2.mutable.Specification

object SubstitutionSpec extends Specification {
  "`jvm_flags` Make variable substitution" should {
    "work" in {
      sys.props.get("annex.jvm_flags.test.makeVariable") must beSome { (value: String) =>
        value.matches("^bazel-out/.*/bin$") must beTrue
      }
    }
  }

  "`jvm_flags` shell expansion" should {
    "work" in {
      sys.props.get("annex.jvm_flags.test.shellExpansion") must beSome("Hello, world!")
    }
  }
}
