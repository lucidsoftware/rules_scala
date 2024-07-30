package higherkindness.rules_scala
package workers.bloop.compile

import common.worker.WorkerMain

import bloop.Bloop
import java.io.PrintStream
import java.nio.file.Path

object BloopRunner extends WorkerMain[Unit] {
  override def init(args: Option[Array[String]]): Unit = ()
  override def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path): Unit = Bloop
}
