package org.jetbrains.skia.impl

expect class NativePointer

expect class InteropPointer

expect abstract class Native(ptr: NativePointer) {
    var _ptr: NativePointer
    open fun _nativeEquals(other: Native?): Boolean

    companion object {
        val NullPointer: NativePointer
    }
}

expect fun reachabilityBarrier(obj: Any?)

fun getPtr(n: Native?): NativePointer = n?._ptr ?: Native.NullPointer

expect class InteropScope() {
    fun toInterop(array: ByteArray?): InteropPointer
    fun byteArrayFromInterop(ptr: InteropPointer): ByteArray?
    fun release()
}

inline fun <T> interopScope(block: InteropScope.() -> T): T {
    val scope = InteropScope()
    try {
        return scope.block()
    } finally {
        scope.release()
    }
}