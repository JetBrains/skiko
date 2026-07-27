package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.Logger

/**
 * Runs a block on the AWT event dispatch thread (EDT) from another thread and blocks until it completes, pumping the
 * calling thread's messages meanwhile so the EDT's cross-thread window ops don't deadlock against the wait. The
 * Windows analog of macOS `LWCToolkit.invokeAndWait`; which message classes get pumped is subtle, see
 * `winApiEdtInvoker.cc`.
 */
internal object WinApiEdtInvoker {
    init {
        Library.load()
    }

    external fun invokeAndWaitWhilePumping(runnable: Runnable)
}

/**
 * Instantiated only from native code, because JNI cannot fabricate a [Runnable] to post onto the EDT.
 * [doneEvent] is a Win32 `HANDLE` passed as a pointer; signaling it releases the pump-waiting thread.
 */
@Suppress("unused")
internal class EdtInvocationTask(
    private val runnable: Runnable,
    private val doneEvent: Long,
) : Runnable {
    override fun run() {
        try {
            runnable.run()
        } catch (t: Throwable) {
            // Don't crash the EDT thread
            Logger.error(t) { "Exception while running task on EDT" }
        } finally {
            signalDone(doneEvent)
        }
    }

    private external fun signalDone(doneEvent: Long)
}
