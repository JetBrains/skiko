package org.jetbrains.skia.impl

import java.lang.ref.Reference

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0L
    }

    override fun toString(): String {
        return javaClass.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

    override fun equals(other: Any?): Boolean {
        return try {
            if (this === other) return true
            if (null == other) return false
            if (!javaClass.isInstance(other)) return false
            val nOther = other as Native
            if (_ptr == nOther._ptr) true else _nativeEquals(nOther)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    // FIXME two different pointers might point to equal objects
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(_ptr)
    }

    actual open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    Reference.reachabilityFence(obj)
}

actual typealias NativePointer = Long

actual typealias InteropPointer = Any?

actual class InteropScope actual constructor() {
    actual fun toInterop(array: ByteArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: ByteArray) {}
    actual fun toInterop(array: FloatArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: FloatArray) {}
    actual fun release() {}
}
