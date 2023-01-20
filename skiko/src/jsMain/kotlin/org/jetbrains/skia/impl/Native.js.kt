package org.jetbrains.skia.impl

internal actual fun toWasm(dest: NativePointer, src: ByteArray): Unit = HEAPU8.set(src, dest)
internal actual fun toWasm(dest: NativePointer, src: ShortArray): Unit = HEAPU16.set(src, dest / 2)
internal actual fun toWasm(dest: NativePointer, src: CharArray): Unit = HEAPU16.set(src, dest / 2)
internal actual fun toWasm(dest: NativePointer, src: FloatArray): Unit = HEAPF32.set(src, dest / 4)
internal actual fun toWasm(dest: NativePointer, src: DoubleArray): Unit = HEAPF64.set(src, dest / 8)
internal actual fun toWasm(dest: NativePointer, src: IntArray): Unit = HEAPU32.set(src, dest / 4)


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