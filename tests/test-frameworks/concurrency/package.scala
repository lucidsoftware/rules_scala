package annex

import java.util.concurrent.CountDownLatch

package object concurrency {
  private val testLatch = new CountDownLatch(4)

  /**
   * All of the tests call this method to wait for each other to have started and printed a message before exiting.
   *
   * This allows us to verify with certainty that the tests' output is buffered, because if it weren't, each would
   * print its "pre" message (the test name), followed by a "SpecX executing..." message, followed by its
   * "post" message. The *only* way to get all the "pre" message printed first and in sequence is if the output is
   * buffered and the "pre" and "post" messages are flushed at the end of the test, after all the
   * "SpecX executing..." messages have been printed.
   */
  def maybeWaitForOthers(): Unit = if (Option(System.getProperty("TEST_MODE")).contains[String]("concurrent")) {
    System.out.flush()
    testLatch.countDown()
  }
}