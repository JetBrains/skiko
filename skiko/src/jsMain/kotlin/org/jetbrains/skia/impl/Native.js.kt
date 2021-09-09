package org.jetbrains.skia.impl

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native?): Boolean = TODO()

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    TODO()
}

actual typealias NativePointer = Int

actual typealias InteropPointer = Int

actual class InteropScope actual constructor() {
    val elements = mutableListOf<NativePointer>()

    actual fun toInterop(array: ByteArray?): InteropPointer {
        return if (array != null) {
            val data = _malloc(array.size)
            elements.add(data)
            toWasm(data, array)
            data
        } else {
            0
        }
    }
    actual fun release()  {
        elements.forEach {
            _free(it)
        }
    }
}

private external fun _malloc(size: Int): NativePointer

private external fun _free(ptr: NativePointer)

private external val HEAPU8: ByteArray

private fun toWasm(dest: NativePointer, src: ByteArray) {
    js("HEAPU8.set(src, dest)")
}

private fun fromWasm(src: NativePointer, size: Int): ByteArray {
    val result = ByteArray(size)
    js("result.set(HEAPU8.subarray(src, size))")
    return result
}
