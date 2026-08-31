package org.jetbrains.skiko

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Creates a [DisplayFrameTicker] backed by the browser's `requestAnimationFrame`. CREATE only &mdash; the
 * caller owns sharing and lifecycle; [close][DisplayFrameTicker.close] it when done. Callbacks run on the
 * browser's animation-frame callback (the main thread); [FrameListener.onFrame] receives `rAF`'s timestamp
 * &mdash; the vsync-aligned frame *start*, not a true predicted present time (see [FrameListener]).
 */
fun DisplayFrameTicker(): DisplayFrameTicker = WebDisplayFrameTicker()

@OptIn(ExperimentalSkikoApi::class)
private class WebDisplayFrameTicker : DisplayFrameTicker {
    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val listeners = mutableListOf<FrameListener>()
    private var scheduled = false
    private var closed = false

    override fun subscribe(listener: FrameListener): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    override fun scheduleFrame() {
        if (closed || scheduled) return
        scheduled = true
        window.requestAnimationFrame { timestampMillis ->
            scheduled = false
            if (closed) return@requestAnimationFrame
            val targetTimeNanos = (timestampMillis * 1_000_000.0).toLong()
            // Snapshot so a listener may (un)subscribe during dispatch.
            scope.launch { for (listener in listeners.toList()) dispatchFrame(listener, targetTimeNanos) }
        }
    }

    override fun close() {
        closed = true
        scope.cancel()
        listeners.clear()
    }
}
