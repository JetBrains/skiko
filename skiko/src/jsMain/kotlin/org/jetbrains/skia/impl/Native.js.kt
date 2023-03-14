package org.jetbrains.skia.impl

import org.khronos.webgl.ArrayBufferView

internal actual fun toWasm(dest: NativePointer, src: ByteArray): Unit = HEAPU8.set(src, dest)
internal actual fun toWasm(dest: NativePointer, src: ShortArray): Unit = HEAPU16.set(src, dest / 2)
internal actual fun toWasm(dest: NativePointer, src: CharArray): Unit = HEAPU16.set(src, dest / 2)
internal actual fun toWasm(dest: NativePointer, src: FloatArray): Unit = HEAPF32.set(src, dest / 4)
internal actual fun toWasm(dest: NativePointer, src: DoubleArray): Unit = HEAPF64.set(src, dest / 8)
internal actual fun toWasm(dest: NativePointer, src: IntArray): Unit = HEAPU32.set(src, dest / 4)

internal external interface HEAP<T> {
    fun set(src: T, dest: NativePointer)
    fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}
internal external object HEAPU8: HEAP<ByteArray> {
    override fun set(src: ByteArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPU16: HEAP<ShortArray> {
    override fun set(src: ShortArray, dest: NativePointer)
    fun set(src: CharArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPU32: HEAP<IntArray> {
    override fun set(src: IntArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPF32: HEAP<FloatArray> {
    override fun set(src: FloatArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPF64: HEAP<DoubleArray> {
    override fun set(src: DoubleArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal actual fun fromWasm(src: NativePointer, result: ByteArray) {
    result.asDynamic().set(HEAPU8.subarray(src, src + result.size))
}

internal actual fun fromWasm(src: NativePointer, result: ShortArray) {
    val startIndex = src / 2
    result.asDynamic().set(HEAPU16.subarray(startIndex, startIndex + result.size))
}

internal actual fun fromWasm(src: NativePointer, result: IntArray) {
    val startIndex = src / 4
    result.asDynamic().set(HEAPU32.subarray(startIndex, startIndex + result.size))
}

internal actual fun fromWasm(src: NativePointer, result: FloatArray) {
    val startIndex = src / 4
    result.asDynamic().set(HEAPF32.subarray(startIndex, startIndex + result.size))
}

internal actual fun fromWasm(src: NativePointer, result: DoubleArray) {
    val startIndex = src / 8
    result.asDynamic().set(HEAPF64.subarray(startIndex, startIndex + result.size))
}

internal actual external fun stringToUTF8(str: String, outPtr: NativePointer, maxBytesToWrite: Int)