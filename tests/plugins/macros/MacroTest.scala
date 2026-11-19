import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class MacroTest {
  @Test def expandsMacroWithTransitiveDependencies(): Unit = {
    val output = new ByteArrayOutputStream()

    Console.withOut(output) {
      Macro.printf("hello %s!", "world")
    }

    assertEquals("hello world!", output.toString("UTF-8"))
  }
}
