package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual typealias AbortCallback = Int

private external fun _registerCallback(cb: () -> Unit, data: Any?): AbortCallback

internal class AbortCallbackValue(@JsName("value") var value: Boolean?)

internal actual fun registerAbortCallback(abort: (() -> Boolean)?): AbortCallback {
    if (abort == null) { return 0 }

    val data = AbortCallbackValue(null)
    return _registerCallback({ data.value = abort() }, data)
}

@ExternalSymbolName("org_jetbrains_skia_Picture__1nPlayback")
internal actual external fun runPlayback(ptr: NativePointer, canvasPtr: NativePointer, abort: AbortCallback)