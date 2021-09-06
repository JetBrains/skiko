package org.jetbrains.skia.impl

actual abstract class Native actual constructor(ptr: Long) {
    actual var _ptr: Long

    actual open fun _nativeEquals(other: Native?): Boolean = TODO()

    actual companion object {
        actual fun getPtr(n: Native?): Long {
            return n?._ptr ?: 0
        }
    }

    init {
        if (ptr == 0L) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual abstract class Native2 actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native2?): Boolean = this._ptr == other?._ptr

    actual companion object {
        actual fun getPtr(n: Native2?): NativePointer {
            return n?._ptr ?: 0
        }
    }

    init {
        if (ptr == 0) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {}

actual typealias InteropHandle = Any

actual typealias InteropPointer = Any

actual typealias NativePointer = Int

actual fun InteropHandle.asInterop(): InteropPointer = this

actual inline fun makeInteropHandle(obj: Any?): InteropHandle? {
    return obj
}

actual inline fun releaseInteropHandle(handle: InteropHandle?) {
}