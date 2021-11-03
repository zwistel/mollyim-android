package org.signal.core.util.concurrent

import java.util.concurrent.Executor

/**
 * An executor that will keep track of the stack trace at the time of calling [execute] and use that to build a more useful stack trace in the event of a crash.
 */
internal class TracingExecutor(val wrapped: Executor) : Executor by wrapped {

  override fun execute(command: Runnable?) {
    val callerStackTrace = Throwable()

    wrapped.execute {
      val currentHandler: Thread.UncaughtExceptionHandler? = Thread.currentThread().uncaughtExceptionHandler
      val originalHandler: Thread.UncaughtExceptionHandler? = if (currentHandler is TracingUncaughtExceptionHandler) currentHandler.originalHandler else currentHandler

      Thread.currentThread().uncaughtExceptionHandler = TracingUncaughtExceptionHandler(originalHandler, callerStackTrace)

      command?.run()
    }
  }
}