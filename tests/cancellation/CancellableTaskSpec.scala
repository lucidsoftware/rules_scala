package anx.cancellation

import higherkindness.rules_scala.common.worker.CancellableTask
import org.scalatest.flatspec.AnyFlatSpec
import java.util.concurrent.ForkJoinPool
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

class CancellableTaskSpec extends AnyFlatSpec {
  "CancellableTask" should "not leak a stale interrupt flag to the next task on the same thread" in {
    val threadPool = new ForkJoinPool(1)
    val executionContext = ExecutionContext.fromExecutor(threadPool)

    try {
      val task1 = CancellableTask((_: () => Boolean) => Thread.currentThread().interrupt())
      val task2 = CancellableTask { (_: () => Boolean) =>
        if (Thread.interrupted()) {
          throw new InterruptedException("Stale interrupt flag leaked from a previous task!")
        }
      }

      task1.execute(executionContext)
      task2.execute(executionContext)

      Await.result(task2.future, Duration.Inf)
    } finally {
      threadPool.shutdown()
    }
  }
}
