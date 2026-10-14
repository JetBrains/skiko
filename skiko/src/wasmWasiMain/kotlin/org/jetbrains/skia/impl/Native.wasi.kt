package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.internal.fastForEach

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    @InternalSkikoApi
    actual val nativePtr: NativePointer
        get() = _ptr

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

@InternalSkikoApi
actual fun reachabilityBarrier(obj: Any?) {}

actual typealias NativePointer = Int

@InternalSkikoApi
actual typealias InteropPointer = Int

@PublishedApi
internal val INTEROP_SCOPE = InteropScope()

@PublishedApi
internal var interopScopeCounter = 0

@InternalSkikoApi
actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
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

@ExternalSymbolName("skiko_malloc")
internal external fun _malloc(size: Int): NativePointer

@ExternalSymbolName("skiko_free")
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

@InternalSkikoApi
actual class InteropScope actual constructor() {
    private val elements = mutableListOf<NativePointer>()

    actual fun toInterop(string: String?): InteropPointer {
        return if (string != null) {
            toInterop(convertToZeroTerminatedString(string))
        } else {
            0
        }
    }

    private fun toInterop(array: ByteArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ByteArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: ShortArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 2)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ShortArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: IntArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: IntArray) {
        fromWasm(this@fromInterop, result)
    }

    actual fun InteropPointer.fromInterop(result: LongArray) {
        TODO("implement wasm fromInterop(LongArray)")
    }

    actual fun toInterop(array: LongArray?): InteropPointer {
        TODO("implement wasm toInterop(LongArray)")
    }

    actual fun InteropPointer.fromInterop(result: FloatArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: FloatArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: DoubleArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: DoubleArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 8)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    private fun toInterop(array: NativePointerArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.size > 0) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array.backing)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: NativePointerArray) {
        return fromWasm(this@fromInterop, result.backing)
    }

    actual fun toInterop(stringArray: Array<String>?): InteropPointer {
        if (stringArray.isNullOrEmpty()) return 0

        val ptrs = IntArray(stringArray.size) { i ->
            toInterop(stringArray[i])
        }
        return toInterop(ptrs)
    }

    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray {
        TODO("implement wasm fromInteropNativePointerArray")
    }

    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> {
        val size = decoder.getArraySize(this)
        val result = Array(size) {
            decoder.getArrayElement(this, it)
        }
        decoder.disposeArray(this)
        return result
    }

    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer {
        return toInterop(interopPointers.toIntArray())
    }

    actual fun booleanCallback(callback: (() -> Boolean)?): NativePointer = 0
    actual fun intCallback(callback: (() -> Int)?): NativePointer = 0
    actual fun nativePointerCallback(callback: (() -> NativePointer)?): NativePointer = 0
    actual fun interopPointerCallback(callback: (() -> InteropPointer)?): NativePointer = 0
    actual fun callback(callback: (() -> Unit)?): InteropPointer = 0
    actual fun virtual(method: () -> Unit): InteropPointer = 0
    actual fun virtualBoolean(method: () -> Boolean): InteropPointer = 0
    actual fun virtualInt(method: () -> Int): InteropPointer = 0
    actual fun virtualNativePointer(method: () -> NativePointer): InteropPointer = 0
    actual fun virtualInteropPointer(method: () -> InteropPointer): InteropPointer = 0

    actual fun release() {
        elements.fastForEach {
            _free(it)
        }
        elements.clear()
    }
}
