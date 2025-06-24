package higherkindness.rules_scala.common.args

import net.sourceforge.argparse4j.inf.Argument
import scala.reflect.Selectable.reflectiveSelectable

object implicits {
  implicit final class SetArgumentDefault(val argument: Argument) extends AnyVal {
    // https://issues.scala-lang.org/browse/SI-2991
    private type SetDefault = { def setDefault(value: AnyRef): Unit }
    def setDefault_[A](value: A): Unit = argument.asInstanceOf[SetDefault].setDefault(value.asInstanceOf[AnyRef])
  }
}
