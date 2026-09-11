package org.jetbrains.skiko

import kotlinx.coroutines.*
import java.awt.event.ActionListener
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.coroutines.CoroutineContext

/**
 * Dispatcher for UI thread, which is used in the current implementation of native UI integration.
 * Currently, it uses Swing event dispatching thread.
 */
val MainUIDispatcher: CoroutineDispatcher
    get() = SwingDispatcher

/**
 * Dispatcher for Swing event dispatching thread.
 *
 * Copy of Dispatchers.Swing from kotlinx-coroutines-swing.
 *
 * We don't depend on kotlinx-coroutines-swing, because it will override Dispatchers.Main, and
 * application can require a different Dispatchers.Main.
 *
 * Note, that we use internal API `Delay` and experimental `resumeUndispatched`.
 * That means it can be changed in the future. When it happens, we need
 * to release a new version of Skiko.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
private object SwingDispatcher : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable): Unit = SwingUtilities.invokeLater(block)

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val timer = schedule(timeMillis, TimeUnit.MILLISECONDS) {
            with(continuation) { resumeUndispatched(Unit) }
        }
        continuation.invokeOnCancellation { timer.stop() }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val timer = schedule(timeMillis, TimeUnit.MILLISECONDS) {
            block.run()
        }
        return object : DisposableHandle {
            override fun dispose() {
                timer.stop()
            }
        }
    }

    private fun schedule(time: Long, unit: TimeUnit, action: ActionListener): Timer =
        Timer(unit.toMillis(time).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), action).apply {
            isRepeats = false
            start()
        }
}