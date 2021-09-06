package org.jetbrains.skia.impl

import java.lang.ref.Reference

actual abstract class Native actual constructor(ptr: Long) {
    actual var _ptr: Long
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

    actual open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    // FIXME two different pointers might point to equal objects
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(_ptr)
    }

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

actual fun reachabilityBarrier(obj: Any?) {
    Reference.reachabilityFence(obj)
}

actual typealias InteropHandle = Any

actual typealias NativePointer = Long
actual typealias InteropPointer = Any

actual abstract class Native2 actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native2?): Boolean = this._ptr == other?._ptr

    actual companion object {
        actual fun getPtr(n: Native2?): NativePointer {
            return n?._ptr ?: 0L
        }
    }

    init {
        if (ptr == 0L) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun InteropHandle.asInterop(): InteropPointer = this

internal actual inline fun makeInteropHandle(obj: Any?): InteropHandle? {
    return obj
}

internal actual inline fun releaseInteropHandle(handle: InteropHandle?) {
    Reference.reachabilityFence(handle)
}