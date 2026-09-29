package annex.jvmflags

object Main {
  def main(arguments: Array[String]): Unit = {
    println(sys.props("annex.jvm_flags.test.makeVariable"))
    println(sys.props("annex.jvm_flags.test.shellExpansion"))
  }
}
