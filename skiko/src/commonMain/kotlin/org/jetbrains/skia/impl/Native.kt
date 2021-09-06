package org.jetbrains.skia.impl

expect abstract class Native(ptr: Long) {
    var _ptr: Long

    companion object {
        fun getPtr(n: Native?): Long
    }

    open fun _nativeEquals(other: Native?): Boolean
}

expect class NativePointer
// TODO: all instances of Native shall be reworked this way.
expect abstract class Native2(ptr: NativePointer) {
    var _ptr: NativePointer

    companion object {
        fun getPtr(n: Native2?): NativePointer
    }

    open fun _nativeEquals(other: Native2?): Boolean
}

expect class InteropPointer

expect class InteropHandle

expect fun InteropHandle.asInterop(): InteropPointer

expect fun reachabilityBarrier(obj: Any?)

internal expect inline fun makeInteropHandle(obj: Any?): InteropHandle?
internal expect inline fun releaseInteropHandle(handle: InteropHandle?)

