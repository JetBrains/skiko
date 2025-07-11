package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other) return false
        if (other !is Native) return false
        return if (_ptr == other._ptr) true else nativeEquals(other)
    }

    override fun hashCode(): Int = _ptr

    internal actual open fun nativeEquals(other: Native?): Boolean {
        return false
    }

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0
    }

    actual override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

internal actual fun reachabilityBarrier(obj: Any?) {}

actual typealias NativePointer = Int
actual typealias InteropPointer = Int

private val INTEROP_SCOPE = InteropScope()
private var interopScopeCounter = 0

internal actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    try {
        interopScopeCounter++
        return INTEROP_SCOPE.block()
    } finally {
        interopScopeCounter--
        if (interopScopeCounter == 0) {
            INTEROP_SCOPE.release()
        }
    }
}


// Those functions are defined by Emscripten.
@ExternalSymbolName("malloc")
internal external fun _malloc(size: Int): NativePointer

@ExternalSymbolName("free")
internal external fun _free(ptr: NativePointer)

actual class NativePointerArray actual constructor(size: Int) {
    internal val backing = IntArray(size)
    actual operator fun get(index: Int): NativePointer {
        return backing[index]
    }

    actual operator fun set(index: Int, value: NativePointer) {
        backing[index] = value
    }

    actual val size: Int
        get() = backing.size

    companion object {
        internal fun fromIntArray(intArray: IntArray): NativePointerArray {
            return NativePointerArray(intArray.size).apply {
                intArray.copyInto(backing)
            }
        }
    }
}

@ExternalSymbolName("skia_memSetByte")
external fun skia_memSetByte(address: NativePointer, value: Byte)

@ExternalSymbolName("skia_memGetByte")
external fun skia_memGetByte(address: NativePointer): Byte

@ExternalSymbolName("skia_memSetChar")
external fun skia_memSetChar(address: NativePointer, value: Char)

@ExternalSymbolName("skia_memGetChar")
external fun skia_memGetChar(address: NativePointer): Char

@ExternalSymbolName("skia_memSetShort")
external fun skia_memSetShort(address: NativePointer, value: Short)

@ExternalSymbolName("skia_memGetShort")
external fun skia_memGetShort(address: NativePointer): Short

@ExternalSymbolName("skia_memSetInt")
external fun skia_memSetInt(address: NativePointer, value: Int)

@ExternalSymbolName("skia_memGetInt")
external fun skia_memGetInt(address: NativePointer): Int

@ExternalSymbolName("skia_memSetFloat")
external fun skia_memSetFloat(address: NativePointer, value: Float)

@ExternalSymbolName("skia_memGetFloat")
external fun skia_memGetFloat(address: NativePointer): Float

@ExternalSymbolName("skia_memSetDouble")
external fun skia_memSetDouble(address: NativePointer, value: Double)

@ExternalSymbolName("skia_memGetDouble")
external fun skia_memGetDouble(address: NativePointer): Double

internal fun toWasm(dest: NativePointer, src: ByteArray) {
    var address = dest
    for (value in src) {
        skia_memSetByte(address, value)
        address += Byte.SIZE_BYTES
    }
}

internal fun toWasm(dest: NativePointer, src: ShortArray) {
    var address = dest
    for (value in src) {
        skia_memSetShort(address, value)
        address += Short.SIZE_BYTES
    }
}

internal fun toWasm(dest: NativePointer, src: CharArray) {
    var address = dest
    for (value in src) {
        skia_memSetChar(address, value)
        address += Char.SIZE_BYTES
    }
}

internal fun toWasm(dest: NativePointer, src: IntArray) {
    var address = dest
    for (value in src) {
        skia_memSetInt(address, value)
        address += Int.SIZE_BYTES
    }
}

internal fun toWasm(dest: NativePointer, src: FloatArray) {
    var address = dest
    for (value in src) {
        skia_memSetFloat(address, value)
        address += Float.SIZE_BYTES
    }
}

internal fun toWasm(dest: NativePointer, src: DoubleArray) {
    var address = dest
    for (value in src) {
        skia_memSetDouble(address, value)
        address += Double.SIZE_BYTES
    }
}

internal fun fromWasm(src: NativePointer, result: ByteArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetByte(address)
        address += Byte.SIZE_BYTES
    }
}

internal fun fromWasm(src: NativePointer, result: ShortArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetShort(address)
        address += Short.SIZE_BYTES
    }
}

internal fun fromWasm(src: NativePointer, result: IntArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetInt(address)
        address += Int.SIZE_BYTES
    }
}

internal fun fromWasm(src: NativePointer, result: FloatArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetFloat(address)
        address += Float.SIZE_BYTES
    }
}

internal fun fromWasm(src: NativePointer, result: DoubleArray) {
    var address = src
    for (index in result.indices) {
        result[index] = skia_memGetDouble(address)
        address += Double.SIZE_BYTES
    }
}

