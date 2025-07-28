package determinism.test

class SampleClass {
  def method1(): String = "hello"
  
  def method2(x: Int, y: String): Boolean = {
    x > 0 && y.nonEmpty
  }
  
  private val field = 42
  
  case class InnerCase(name: String, value: Int)
  
  object InnerObject {
    def compute(): Int = field * 2
  }
}
