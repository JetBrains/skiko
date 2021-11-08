package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import java.util.function.BooleanSupplier as JBooleanSupplier

actual typealias AbortCallback = AbortCallbackImpl?

class AbortCallbackImpl(val callback: () -> Boolean): JBooleanSupplier {
    override fun getAsBoolean(): Boolean {
        return callback()
    }
}

internal actual fun registerAbortCallback(abort: (() -> Boolean)?): AbortCallback {
    return abort?.let { AbortCallbackImpl(it) }
}

internal actual external fun runPlayback(ptr: NativePointer, canvasPtr: NativePointer, abort: AbortCallback)