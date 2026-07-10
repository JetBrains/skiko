package org.jetbrains.skiko

import android.view.Choreographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Creates a [DisplayFrameTicker] backed by the platform [Choreographer]. Must be created on a thread with a
 * `Looper` (typically the main thread); callbacks are delivered on that thread. CREATE only &mdash; the
 * caller owns sharing and lifecycle; [close][DisplayFrameTicker.close] it when done.
 *
 * [FrameListener.onFrame] receives `Choreographer`'s `frameTimeNanos` &mdash; the vsync-aligned frame
 * *start*, not a true predicted present time (see [FrameListener]).
 */
fun DisplayFrameTicker(): DisplayFrameTicker = AndroidDisplayFrameTicker()

@OptIn(ExperimentalSkikoApi::class)
private class AndroidDisplayFrameTicker : DisplayFrameTicker, Choreographer.FrameCallback {
    private val choreographer = Choreographer.getInstance()
    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val listeners = CopyOnWriteArrayList<FrameListener>()
    private var scheduled = false
    private var closed = false

    override fun subscribe(listener: FrameListener): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    override fun scheduleFrame() {
        if (closed || scheduled) return
        scheduled = true
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        scheduled = false
        if (closed) return
        scope.launch {
            for (listener in listeners) dispatchFrame(listener, frameTimeNanos)
        }
    }

    override fun close() {
        closed = true
        choreographer.removeFrameCallback(this)
        scope.cancel()
        listeners.clear()
    }
}
