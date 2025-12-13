package org.jetbrains.skiko.binding

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import org.gtk.GCancellable
import org.gtk.g_cancellable_connect
import org.gtk.g_cancellable_new

class NativeCancellable : CHandled<GCancellable>(g_cancellable_new()) {

    fun onCancel(block: () -> Unit) {
        val blockStableRef = StableRef.create(block)
        val callback = staticCFunction { blockRefPtr: CPointer<*> ->
            blockRefPtr.asStableRef<() -> Unit>().also {
                it.get().invoke()
                it.dispose()
            }
        }
        g_cancellable_connect(
            handle,
            callback.reinterpret(),
            blockStableRef.asCPointer(),
            null
        )
    }
}