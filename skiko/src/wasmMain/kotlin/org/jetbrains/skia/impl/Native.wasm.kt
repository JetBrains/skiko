package org.jetbrains.skia.impl

@kotlin.wasm.WasmImport("skia", "skia_memSetByte")
external fun skia_memSetByte(address: NativePointer, value: Byte)

@kotlin.wasm.WasmImport("skia", "skia_memGetByte")
external fun skia_memGetByte(address: NativePointer): Byte

@kotlin.wasm.WasmImport("skia", "skia_memSetChar")
external fun skia_memSetChar(address: NativePointer, value: Char)

@kotlin.wasm.WasmImport("skia", "skia_memGetChar")
external fun skia_memGetChar(address: NativePointer): Char

@kotlin.wasm.WasmImport("skia", "skia_memSetShort")
external fun skia_memSetShort(address: NativePointer, value: Short)

@kotlin.wasm.WasmImport("skia", "skia_memGetShort")
external fun skia_memGetShort(address: NativePointer): Short

@kotlin.wasm.WasmImport("skia", "skia_memSetInt")
external fun skia_memSetInt(address: NativePointer, value: Int)

@kotlin.wasm.WasmImport("skia", "skia_memGetInt")
external fun skia_memGetInt(address: NativePointer): Int

@kotlin.wasm.WasmImport("skia", "skia_memSetFloat")
external fun skia_memSetFloat(address: NativePointer, value: Float)

@kotlin.wasm.WasmImport("skia", "skia_memGetFloat")
external fun skia_memGetFloat(address: NativePointer): Float

@kotlin.wasm.WasmImport("skia", "skia_memSetDouble")
external fun skia_memSetDouble(address: NativePointer, value: Double)

@kotlin.wasm.WasmImport("skia", "skia_memGetDouble")
external fun skia_memGetDouble(address: NativePointer): Double

internal actual fun toWasm(dest: NativePointer, src: ByteArray) {
    var address = dest
    for (value in src) {
        skia_memSetByte(address, value)
        address += Byte.SIZE_BYTES
    }
}

internal actual fun toWasm(dest: NativePointer, src: ShortArray) {
    var address = dest
    for (value in src) {
        skia_memSetShort(address, value)
        address += Short.SIZE_BYTES
    }
}

internal actual fun toWasm(dest: NativePointer, src: CharArray) {
    var address = dest
    for (value in src) {
        skia_memSetChar(address, value)
        address += Char.SIZE_BYTES
    }
}

internal actual fun toWasm(dest: NativePointer, src: IntArray) {
    var address = dest
    for (value in src) {
        skia_memSetInt(address, value)
        address += Int.SIZE_BYTES
    }
}

internal actual fun toWasm(dest: NativePointer, src: FloatArray) {
    var address = dest
    for (value in src) {
        skia_memSetFloat(address, value)
        address += Float.SIZE_BYTES
    }
}

internal actual fun toWasm(dest: NativePointer, src: DoubleArray) {
    var address = dest
    for (value in src) {
        skia_memSetDouble(address, value)
        address += Double.SIZE_BYTES
    }
}

internal actual fun fromWasm(src: NativePointer, result: ByteArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetByte(address)
        address += Byte.SIZE_BYTES
    }
}

internal actual fun fromWasm(src: NativePointer, result: ShortArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetShort(address)
        address += Short.SIZE_BYTES
    }
}

internal actual fun fromWasm(src: NativePointer, result: IntArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetInt(address)
        address += Int.SIZE_BYTES
    }
}

internal actual fun fromWasm(src: NativePointer, result: FloatArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetFloat(address)
        address += Float.SIZE_BYTES
    }
}

internal actual fun fromWasm(src: NativePointer, result: DoubleArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetDouble(address)
        address += Double.SIZE_BYTES
    }
}

internal actual fun stringToUTF8(str: String, outPtr: NativePointer, maxBytesToWrite: Int) {
    if (maxBytesToWrite <= 0) return

    val utf8 = str.encodeToByteArray()
    val lastIndex = minOf(maxBytesToWrite - 1, utf8.size)

    var index = 0
    while (index < lastIndex) {
        skia_memSetByte(outPtr + index, utf8[index])
        index++
    }
    skia_memSetByte(outPtr + index, 0)
}