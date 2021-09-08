package org.jetbrains.skia.impl

import kotlin.native.internal.NativePtr

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer
    override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x$_ptr)"
    }

    actual open fun _nativeEquals(other: Native?): Boolean = this._ptr == other?._ptr ?: Native.NullPointer

    actual companion object {
        actual val NullPointer: NativePointer
            get() = NativePtr.NULL
    }

    init {
        if (ptr == NativePtr.NULL) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual typealias NativePointer = NativePtr

actual fun reachabilityBarrier(obj: Any?) {
    // TODO: implement native barrier
}
