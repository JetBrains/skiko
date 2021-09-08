package org.jetbrains.skia.impl

import org.jetbrains.skia.IPoint

expect class NativePointer

expect abstract class Native(ptr: NativePointer) {
    var _ptr: NativePointer
    open fun _nativeEquals(other: Native?): Boolean

    companion object {
        val NullPointer: NativePointer
    }
}

expect fun reachabilityBarrier(obj: Any?)

fun getPtr(n: Native?): NativePointer = n?._ptr ?: Native.NullPointer
