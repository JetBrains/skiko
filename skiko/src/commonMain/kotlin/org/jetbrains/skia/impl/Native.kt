package org.jetbrains.skia.impl

expect class NativePointer

expect fun Int.toNativePointer(): NativePointer

expect abstract class Native(ptr: NativePointer) {
    var _ptr: NativePointer
    open fun _nativeEquals(other: Native?): Boolean

    companion object {
        val NULLPNTR: NativePointer
    }
}

expect fun reachabilityBarrier(obj: Any?)

fun getPtr(n: Native?): NativePointer = n?._ptr ?: Native.NULLPNTR

