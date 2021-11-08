package org.jetbrains.skia

import kotlinx.cinterop.*
import org.jetbrains.skia.impl.NativePointer

actual class AbortCallback(val callbackRef: StableRef<KtAbortCallback>?)

internal actual fun registerAbortCallback(abort: (() -> Boolean)?): AbortCallback {
    val data = abort?.let { StableRef.create(KtAbortCallback(it)) }
    return AbortCallback(data)
}

class KtAbortCallback(val callback: () -> Boolean) {
    fun abort(): Boolean = callback()
}

fun callAbortCallback(ptr: COpaquePointer): Boolean {
    return ptr.asStableRef<KtAbortCallback>().get().abort()
}

fun disposeAbortCallback(ptr: COpaquePointer) {
    ptr.asStableRef<KtAbortCallback>().dispose()
}

internal actual fun runPlayback(ptr: NativePointer, canvasPtr: NativePointer, abort: AbortCallback) {
    val cb = staticCFunction(::callAbortCallback)
    val drop = staticCFunction(::disposeAbortCallback)
    _nPlayback(ptr, canvasPtr, cb, drop, data = abort.callbackRef?.asCPointer())
}


@ExternalSymbolName("org_jetbrains_skia_Picture__1nPlayback")
private external fun _nPlayback(ptr: NativePointer,
                                canvasPtr: NativePointer,
                                cb: CPointer<CFunction<(COpaquePointer)->Boolean>>,
                                drop: CPointer<CFunction<(COpaquePointer) -> Unit>>,
                                data: COpaquePointer?)