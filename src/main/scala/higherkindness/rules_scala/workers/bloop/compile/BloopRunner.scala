package higherkindness.rules_scala.workers.bloop.compile

import bloop.Bloop
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}

object BloopRunner extends WorkerMain[Unit] {
  override def init(args: Option[Array[String]]): Unit = ()
  override def work(workRequest: WorkTask[Unit]): Unit = Bloop
}
