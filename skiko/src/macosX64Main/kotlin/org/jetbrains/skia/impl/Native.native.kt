package org.jetbrains.skia.impl

import kotlinx.cinterop.*

actual abstract class Native actual constructor(ptr: Long) {
    actual var _ptr: Long
    override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

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

actual fun reachabilityBarrier(obj: Any?) {}

actual class InteropHandle(internal val ref: StableRef<Any>?)

actual abstract class Native2 actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native2?): Boolean = this._ptr == other?._ptr

    actual companion object {
        actual fun getPtr(n: Native2?): NativePointer {
            return n?._ptr ?: NativePointer.NULL
        }
    }

    init {
        if (ptr == NativePointer.NULL) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual typealias NativePointer = kotlin.native.internal.NativePtr
actual typealias InteropPointer = kotlin.native.internal.NativePtr

actual fun InteropHandle.asInterop(): InteropPointer = this.ref?.asCPointer().rawValue

actual inline fun makeInteropHandle(obj: Any?): InteropHandle? {
    return obj?.let { InteropHandle( StableRef.create(it)) }
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
actual inline fun releaseInteropHandle(handle: InteropHandle?) {
    handle?.ref?.dispose()
}