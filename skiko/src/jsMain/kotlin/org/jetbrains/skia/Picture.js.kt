package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual typealias AbortCallback = Int

private external fun _registerCallback(cb: () -> Unit, data: Any?): AbortCallback

internal actual fun registerAbortCallback(abort: (() -> Boolean)?): AbortCallback {
    if (abort == null) { return 0 }

    val data = arrayOf<Boolean?>(null) // We use array instead of structs due to field name mangling
    return _registerCallback({ data[0] = abort() }, data)
}

@ExternalSymbolName("org_jetbrains_skia_Picture__1nPlayback")
internal actual external fun runPlayback(ptr: NativePointer, canvasPtr: NativePointer, abort: AbortCallback)