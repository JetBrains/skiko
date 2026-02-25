@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skia.webext

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl._malloc
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.await
import org.jetbrains.skiko.wasm.awaitSkiko
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.JsAny
import kotlin.js.js

internal fun skikoArrayBuffer(skikoWasm: JsAny): ArrayBuffer =
    js("skikoWasm.wasmExports.memory.buffer")

internal suspend fun copyBufferToSkiko(
    src: ArrayBuffer
): NativePointer {
    val ptr = _malloc(src.byteLength)
    if (ptr != Native.NullPointer) {
        val skikoArrayBuffer = skikoArrayBuffer(awaitSkiko.await())
        copyBuffer(src, skikoArrayBuffer, ptr)
    }
    return ptr
}

internal fun copyBuffer(
    src: ArrayBuffer,
    dst: ArrayBuffer,
    dstOffset: Int
) {
    val dstView = Uint8Array(dst)
    val srcView = Uint8Array(src)
    dstView.set(srcView, dstOffset)
}