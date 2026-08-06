package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.Logger
import java.awt.EventQueue

/**
 * Runs a block on the AWT event dispatch thread (EDT) from another thread and blocks until it completes, pumping
 * the calling thread's messages meanwhile so the EDT's cross-thread window ops don't deadlock against the wait.
 *
 * This is Windows analog of the macOS `LWCToolkit.invokeAndWait`.
 *
 * See `winApiEdtInvoker.cc` for details.
 */
internal object WinApiEdtInvoker {
    init {
        Library.load()
    }

    /**
     * Runs [runnable] on the EDT and pumps messages posted to this thread until it completes.
     */
    fun invokeAndWaitWhilePumping(runnable: Runnable) {
        val doneEvent = preparePumping().also {
            require(it != 0L) { "Unable to run the task on the EDT" }
        }

        EventQueue.invokeLater {
            try {
                runnable.run()
            } catch (t: Throwable) {
                // Don't crash the EDT thread
                Logger.error(t) { "Exception while running task on EDT" }
            } finally {
                signalDone(doneEvent)
            }
        }
        pumpUntilDone(doneEvent)
    }

    /**
     * Creates the Win32 event that ends the pump and returns its `HANDLE`, or 0 if it couldn't be created.
     * Call [signalDone] on the return value to signal to the native code to end pumping.
     */
    private external fun preparePumping(): Long

    /**
     * Pumps messages posted to the thread until [doneEvent] signals to stop.
     */
    private external fun pumpUntilDone(doneEvent: Long)

    /**
     * Signals via [doneEvent] to stop pumping.
     */
    private external fun signalDone(doneEvent: Long)
}
