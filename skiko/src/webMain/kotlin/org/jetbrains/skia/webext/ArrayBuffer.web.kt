@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skia.webext

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl._malloc
import org.jetbrains.skiko.ExperimentalSkikoApi
import kotlin.js.js

internal fun skikoArrayBuffer(skikoWasm: SkikoWasm): WebArrayBufferExt =
    js("skikoWasm.wasmExports.memory.buffer")

/**
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer
 */
@ExperimentalSkikoApi
external interface WebArrayBufferExt {
    val byteLength: Int
    operator fun set(ix: Int, value: Byte)
}
internal external interface SkikoWasm

internal suspend fun copyBufferToSkiko(
    src: WebArrayBufferExt
): NativePointer {
    val ptr = _malloc(src.byteLength)
    if (ptr != Native.NullPointer) {
        val skikoArrayBuffer = skikoArrayBuffer(getSkikoWasm())
        copyBuffer(src, skikoArrayBuffer, src.byteLength, ptr)
    }
    return ptr
}

internal expect suspend fun getSkikoWasm(): SkikoWasm

internal fun copyBuffer(
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