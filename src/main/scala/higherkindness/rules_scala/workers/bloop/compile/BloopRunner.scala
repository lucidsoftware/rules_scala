package higherkindness.rules_scala
package workers.bloop.compile

import common.worker.{WorkTask, WorkerMain}

import bloop.Bloop
import java.io.PrintStream
import java.nio.file.Path

object BloopRunner extends WorkerMain[Unit] {
  override def init(args: Option[Array[String]]): Unit = ()
  override def work(workRequest: WorkTask[Unit]): Unit = Bloop
}
