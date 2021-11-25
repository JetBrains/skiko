package org.jetbrains.skia.impl

import org.khronos.webgl.ArrayBufferView

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other) return false
        if (other !is Native) return false
        return if (_ptr == other._ptr) true else _nativeEquals(other)
    }

    override fun hashCode(): Int = _ptr

    actual open fun _nativeEquals(other: Native?): Boolean {
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

actual fun reachabilityBarrier(obj: Any?) {
    // TODO: impl later
}

actual typealias NativePointer = Int
actual typealias InteropPointer = Int

actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    val scope = InteropScope()
    try {
        return scope.block()
    } finally {
        scope.release()
    }
}

actual class InteropScope actual constructor() {
    private val elements = mutableListOf<NativePointer>()
    private var callbacksInitialized = false

    actual fun toInterop(string: String?): InteropPointer {
        return if (string != null) {
            val data = _malloc(string.length * 4)
            stringToUTF8(string, data, string.length * 4)
            elements.add(data)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: CharArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInterop(result: CharArray) {
        val tmp = UTF8ToString(this@fromInterop)
        tmp.toCharArray().copyInto(result)
    }

    actual fun toInterop(array: ByteArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }
    actual fun InteropPointer.fromInterop(result: ShortArray) {
        fromWasm(this@fromInterop, result)
    }

    actual fun toInterop(array: ShortArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 2)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInterop(result: IntArray) {
        fromWasm(this@fromInterop, result)
    }

    actual fun toInterop(array: IntArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
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

    actual fun toInterop(array: FloatArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInterop(result: DoubleArray) {
        fromWasm(this@fromInterop, result)
    }

    actual fun toInterop(array: DoubleArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 8)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInterop(result: ByteArray) {
        fromWasm(this@fromInterop, result)
    }

    actual fun toInterop(array: NativePointerArray?): InteropPointer {
        return if (array != null && array.size > 0) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            toWasm(data, array.backing)
            data
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInterop(result: NativePointerArray) {
        return fromWasm(this@fromInterop, result.backing)
    }

    actual fun toInterop(stringArray: Array<String>?): InteropPointer {
        return if (stringArray != null && stringArray.isNotEmpty()) {
            val ptrs = stringArray.map {
                toInterop(it)
            }.toIntArray()

            toInterop(ptrs)
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray {
        TODO("implement wasm fromInteropNativePointerArray")
    }

    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> {
        val size = decoder.getArraySize(this)
        val result = Array<T>(size) {
            decoder.getArrayElement(this, it)
        }
        decoder.disposeArray(this)
        return result
    }

    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer {
        return toInterop(interopPointers.toIntArray())
    }

    actual fun booleanCallback(callback: (() -> Boolean)?): InteropPointer {
        if (callback == null) { return 0 }
        initCallbacks()

        val data = CallbackData<Boolean>(null)
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun callback(callback: (() -> Unit)?): InteropPointer {
        if (callback == null) { return 0 }
        initCallbacks()

        return _registerCallback({ callback() }, null, global = false)
    }

    actual fun virtual(method: () -> Unit): InteropPointer {
        return _registerCallback({ method() }, null, global = true)
    }

    actual fun virtualBoolean(method: () -> Boolean): InteropPointer {
        val data = CallbackData<Boolean>(null)
        return _registerCallback({ data.value = method() }, data, global = true);
    }

    actual fun release()  {
        elements.forEach {
            _free(it)
        }
        releaseCallbacks()
    }

    private inline fun initCallbacks() {
        if (!callbacksInitialized) {
            _createLocalCallbackScope()
            callbacksInitialized = true
        }
    }

    private inline fun releaseCallbacks() {
        if (callbacksInitialized) {
            _releaseLocalCallbackScope()
            callbacksInitialized = false
        }
    }
}

// Callbacks
private class CallbackData<T>(@JsName("value") var value: T?)

// See `setup.js`
private external fun _registerCallback(cb: () -> Unit, data: Any?, global: Boolean): Int
private external fun _createLocalCallbackScope()
private external fun _releaseLocalCallbackScope()


// Those functions are defined by Emscripten.
private external fun _malloc(size: Int): NativePointer

private external fun _free(ptr: NativePointer)

private external fun lengthBytesUTF8(str: String): Int

private external fun stringToUTF8(str: String, outPtr: NativePointer, maxBytesToWrite: Int)

private external fun UTF8ToString(ptr: NativePointer): String

private external interface HEAP<T> {
    fun set(src: T, dest: NativePointer)
    fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}
private external object HEAPU8: HEAP<ByteArray> {
    override fun set(src: ByteArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

private external object HEAPU16: HEAP<ShortArray> {
    override fun set(src: ShortArray, dest: NativePointer)
    fun set(src: CharArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

private external object HEAPU32: HEAP<IntArray> {
    override fun set(src: IntArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

private external object HEAPF32: HEAP<FloatArray> {
    override fun set(src: FloatArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

private external object HEAPF64: HEAP<DoubleArray> {
    override fun set(src: DoubleArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

// Data copying routines.
private fun toWasm(dest: NativePointer, src: ByteArray): Unit = HEAPU8.set(src, dest)
private fun toWasm(dest: NativePointer, src: ShortArray): Unit = HEAPU16.set(src, dest / 2)
private fun toWasm(dest: NativePointer, src: CharArray): Unit = HEAPU16.set(src, dest / 2)
private fun toWasm(dest: NativePointer, src: FloatArray): Unit = HEAPF32.set(src, dest / 4)
private fun toWasm(dest: NativePointer, src: DoubleArray): Unit = HEAPF64.set(src, dest / 8)
private fun toWasm(dest: NativePointer, src: IntArray): Unit = HEAPU32.set(src, dest / 4)

private fun fromWasm(src: NativePointer, result: ByteArray) {
    result.asDynamic().set(HEAPU8.subarray(src, src + result.size))
}

private fun fromWasm(src: NativePointer, result: ShortArray) {
    val startIndex = src / 2
    result.asDynamic().set(HEAPU16.subarray(startIndex, startIndex + result.size))
}

private fun fromWasm(src: NativePointer, result: IntArray) {
    val startIndex = src / 4
    result.asDynamic().set(HEAPU32.subarray(startIndex, startIndex + result.size))
}

private fun fromWasm(src: NativePointer, result: FloatArray) {
    val startIndex = src / 4
    result.asDynamic().set(HEAPF32.subarray(startIndex, startIndex + result.size))
}

private fun fromWasm(src: NativePointer, result: DoubleArray) {
    val startIndex = src / 8
    result.asDynamic().set(HEAPF64.subarray(startIndex, startIndex + result.size))
}

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
