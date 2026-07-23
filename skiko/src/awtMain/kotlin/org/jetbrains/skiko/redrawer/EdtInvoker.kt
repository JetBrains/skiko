package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.Logger

/**
 * Runs a block on the AWT event dispatch thread (EDT) from another thread — e.g. the Windows toolkit thread
 * that drives a live resize — and blocks the caller until the block completes, pumping the calling thread's
 * sent and posted messages meanwhile so the EDT's own cross-thread window ops (marshaled back to the calling thread)
 * complete instead of deadlocking against the wait.
 *
 * The Windows analog of macOS `LWCToolkit.invokeAndWait`, which has no JDK equivalent. The wait event and the
 * message pump are handled entirely natively; see `edtInvoker.cc`.
 */
internal object EdtInvoker {
    init {
        Library.load()
    }

    /**
     * Posts [runnable] to the EDT (via `EventQueue.invokeLater`) and blocks the calling thread until it has run.
     */
    external fun invokeAndWaitWhilePumping(runnable: Runnable)
}

/**
 * The one Java shim [EdtInvoker] needs: JNI cannot fabricate a [Runnable] to post onto the EDT, so the native
 * `invokeAndWaitWhilePumping` constructs this. Its [run] invokes [runnable] and then signals the native
 * completion event [doneEvent] (a Win32 `HANDLE` passed as a pointer), releasing the pump-waiting thread.
 * Only ever instantiated from native code.
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
