package org.jetbrains.skia.webext

import kotlinx.coroutines.await
import org.jetbrains.skiko.wasm.awaitSkiko

internal actual suspend fun getSkikoWasm(): SkikoWasm {
    return awaitSkiko.await()
}

internal actual fun skikoArrayBuffer(skikoWasm: SkikoWasm): WebArrayBufferExt =
    js("skikoWasm.wasmExports.memory.buffer")

internal actual fun copyBuffer(
    src: WebArrayBufferExt,
    dst: WebArrayBufferExt,
    size: Int,
    dstOffset: Int
) {
    js("""
        var dstView = new Uint8Array(dst);
        var srcView = new Uint8Array(src);
        dstView.set(srcView, dstOffset);
    """)
}