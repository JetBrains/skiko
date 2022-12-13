package org.jetbrains.skia.impl

@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<ByteArray>, index: Int, value: Byte)
@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<ShortArray>, index: Int, value: Char)
@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<ShortArray>, index: Int, value: Short)
@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<IntArray>, index: Int, value: Int)
@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<FloatArray>, index: Int, value: Float)
@JsFun("(dest, index, value) => dest[index] = value")
private external fun set(dest: HEAP<DoubleArray>, index: Int, value: Double)

internal actual fun toWasm(dest: NativePointer, src: ByteArray) {
    var index = dest / Byte.SIZE_BYTES
    for (value in src) {
        set(HEAPU8, index, value)
        index++
    }
}

internal actual fun toWasm(dest: NativePointer, src: ShortArray) {
    var index = dest / Short.SIZE_BYTES
    for (value in src) {
        set(HEAPU16, index, value)
        index++
    }
}
internal actual fun toWasm(dest: NativePointer, src: CharArray) {
    var index = dest / Char.SIZE_BYTES
    for (value in src) {
        set(HEAPU16, index, value.code.toShort())
        index++
    }
}

internal actual fun toWasm(dest: NativePointer, src: IntArray) {
    var index = dest / Int.SIZE_BYTES
    for (value in src) {
        set(HEAPU32, index, value)
        index++
    }
}

internal actual fun toWasm(dest: NativePointer, src: FloatArray) {
    var index = dest / Float.SIZE_BYTES
    for (value in src) {
        set(HEAPF32, index, value)
        index++
    }
}

internal actual fun toWasm(dest: NativePointer, src: DoubleArray) {
    var index = dest / Double.SIZE_BYTES
    for (value in src) {
        set(HEAPF64, index, value)
        index++
    }
}

@JsFun("(dest, index) => dest[index]")
private external fun get(dest: HEAP<ByteArray>, index: Int): Byte
@JsFun("(dest, index) => dest[index]")
private external fun get(dest: HEAP<ShortArray>, index: Int): Short
@JsFun("(dest, index) => dest[index]")
private external fun get(dest: HEAP<IntArray>, index: Int): Int
@JsFun("(dest, index) => dest[index]")
private external fun get(dest: HEAP<FloatArray>, index: Int): Float
@JsFun("(dest, index) => dest[index]")
private external fun get(dest: HEAP<DoubleArray>, index: Int): Double

internal actual fun fromWasm(src: NativePointer, result: ByteArray) {
    val index = src / Byte.SIZE_BYTES
    for (i in result.indices) {
        result[i] = get(HEAPU8, index + i)
    }
}

internal actual fun fromWasm(src: NativePointer, result: ShortArray) {
    val index = src / Short.SIZE_BYTES
    for (i in result.indices) {
        result[i] = get(HEAPU16, index + i)
    }
}

internal actual fun fromWasm(src: NativePointer, result: IntArray) {
    val index = src / Int.SIZE_BYTES
    for (i in result.indices) {
        result[i] = get(HEAPU32, index + i)
    }
}

internal actual fun fromWasm(src: NativePointer, result: FloatArray) {
    val index = src / Float.SIZE_BYTES
    for (i in result.indices) {
        result[i] = get(HEAPF32, index + i)
    }
}

internal actual fun fromWasm(src: NativePointer, result: DoubleArray) {
    val index = src / Double.SIZE_BYTES
    for (i in result.indices) {
        result[i] = get(HEAPF64, index + i)
    }
}