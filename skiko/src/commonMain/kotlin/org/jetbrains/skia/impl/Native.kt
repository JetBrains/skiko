package org.jetbrains.skia.impl

expect class NativePointer

expect class InteropPointer

expect abstract class Native(ptr: NativePointer) {
    var _ptr: NativePointer
    open fun _nativeEquals(other: Native?): Boolean

    companion object {
        val NullPointer: NativePointer
    }

    override fun toString(): String
}

expect fun reachabilityBarrier(obj: Any?)

fun getPtr(n: Native?): NativePointer = n?._ptr ?: Native.NullPointer

expect class InteropScope() {
    fun toInterop(string: String?): InteropPointer
    fun InteropPointer.fromInterop(result: CharArray)
    fun toInterop(array: ByteArray?): InteropPointer
    fun InteropPointer.fromInterop(result: ByteArray)
    fun toInterop(array: FloatArray?): InteropPointer
    fun InteropPointer.fromInterop(result: FloatArray)
    fun toInterop(array: NativePointerArray?): InteropPointer

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

inline fun withResult(result: ByteArray, block: (InteropPointer) -> Unit): ByteArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withResult(result: FloatArray, block: (InteropPointer) -> Unit): FloatArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

