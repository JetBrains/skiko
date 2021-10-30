package org.jetbrains.skia.impl

import org.jetbrains.skia.ManagedString

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
    fun toInterop(array: ShortArray?): InteropPointer
    fun InteropPointer.fromInterop(result: ShortArray)
    fun toInterop(array: IntArray?): InteropPointer
    fun InteropPointer.fromInterop(result: IntArray)
    fun toInterop(array: LongArray?): InteropPointer
    fun InteropPointer.fromInterop(result: LongArray)
    fun toInterop(array: FloatArray?): InteropPointer
    fun InteropPointer.fromInterop(result: FloatArray)
    fun toInterop(array: DoubleArray?): InteropPointer
    fun InteropPointer.fromInterop(result: DoubleArray)
    fun toInterop(array: NativePointerArray?): InteropPointer
    fun InteropPointer.fromInterop(result: NativePointerArray)
    fun toInterop(stringArray: Array<String>?): InteropPointer
    fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray
    inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T>
    fun release()
}

expect inline fun <T> interopScope(block: InteropScope.() -> T): T

inline fun withResult(result: ByteArray, block: (InteropPointer) -> Unit): ByteArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withNullableResult(result: ByteArray, block: (InteropPointer) -> Boolean): ByteArray? = interopScope {
    val handle = toInterop(result)
    return if (block(handle)) {
        handle.fromInterop(result)
        result
    } else {
        null
    }
}

inline fun withResult(result: FloatArray, block: (InteropPointer) -> Unit): FloatArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withResult(result: IntArray, block: (InteropPointer) -> Unit): IntArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withNullableResult(result: IntArray, block: (InteropPointer) -> Boolean): IntArray? = interopScope {
    val handle = toInterop(result)
    return if (block(handle)) {
        handle.fromInterop(result)
        result
    } else {
        null
    }
}

inline fun withResult(result: ShortArray, block: (InteropPointer) -> Unit): ShortArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withResult(result: DoubleArray, block: (InteropPointer) -> Unit): DoubleArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

inline fun withResult(result: NativePointerArray, block: (InteropPointer) -> Unit): NativePointerArray = interopScope {
    val handle = toInterop(result)
    block(handle)
    handle.fromInterop(result)
    result
}

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun withStringResult(block: () -> NativePointer): String {
    val string = ManagedString(block())
    return string.toString()
}

interface ArrayInteropDecoder<T> {
    fun getArrayElement(array: InteropPointer, index: Int): T
    fun getArraySize(array: InteropPointer): Int
    fun disposeArray(array: InteropPointer)
}
