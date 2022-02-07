package org.jetbrains.skia.impl

import org.jetbrains.skia.ManagedString

expect class NativePointer

expect class InteropPointer

expect abstract class Native(ptr: NativePointer) {
    internal var _ptr: NativePointer
    internal open fun nativeEquals(other: Native?): Boolean

    companion object {
        val NullPointer: NativePointer
    }

    override fun toString(): String
}

internal expect fun reachabilityBarrier(obj: Any?)

internal fun getPtr(n: Native?): NativePointer = n?._ptr ?: Native.NullPointer

internal expect class InteropScope() {
    fun toInterop(string: String?): InteropPointer

    fun toInterop(array: ByteArray?): InteropPointer
    fun toInteropForResult(array: ByteArray?): InteropPointer
    fun InteropPointer.fromInterop(result: ByteArray)

    fun toInterop(array: ShortArray?): InteropPointer
    fun toInteropForResult(array: ShortArray?): InteropPointer
    fun InteropPointer.fromInterop(result: ShortArray)

    fun toInterop(array: IntArray?): InteropPointer
    fun toInteropForResult(array: IntArray?): InteropPointer
    fun InteropPointer.fromInterop(result: IntArray)

    fun toInterop(array: LongArray?): InteropPointer
    fun InteropPointer.fromInterop(result: LongArray)

    fun toInterop(array: FloatArray?): InteropPointer
    fun toInteropForResult(array: FloatArray?): InteropPointer
    fun InteropPointer.fromInterop(result: FloatArray)

    fun toInterop(array: DoubleArray?): InteropPointer
    fun toInteropForResult(array: DoubleArray?): InteropPointer
    fun InteropPointer.fromInterop(result: DoubleArray)

    fun toInterop(array: NativePointerArray?): InteropPointer
    fun toInteropForResult(array: NativePointerArray?): InteropPointer
    fun InteropPointer.fromInterop(result: NativePointerArray)

    fun toInterop(stringArray: Array<String>?): InteropPointer

    fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray
    inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T>
    fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer

    // Callbacks
    fun callback(callback: (() -> Unit)?): InteropPointer
    fun intCallback(callback: (() -> Int)?): InteropPointer
    fun nativePointerCallback(callback: (() -> NativePointer)?): InteropPointer
    fun interopPointerCallback(callback: (() -> InteropPointer)?): InteropPointer
    fun booleanCallback(callback: (() -> Boolean)?): InteropPointer

    // Virtual methods
    fun virtual(method: () -> Unit): InteropPointer
    fun virtualInt(method: () -> Int): InteropPointer
    fun virtualNativePointer(method: () -> NativePointer): InteropPointer
    fun virtualInteropPointer(method: () -> InteropPointer): InteropPointer
    fun virtualBoolean(method: () -> Boolean): InteropPointer

    fun release()
}

internal expect inline fun <T> interopScope(block: InteropScope.() -> T): T

internal inline fun withResult(result: ByteArray, block: InteropScope.(InteropPointer) -> Unit): ByteArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}

internal inline fun withNullableResult(result: ByteArray, block: InteropScope.(InteropPointer) -> Boolean): ByteArray? = interopScope {
    val handle = toInteropForResult(result)
    return if (block(handle)) {
        handle.fromInterop(result)
        result
    } else {
        null
    }
}

internal inline fun withResult(result: FloatArray, block: InteropScope.(InteropPointer) -> Unit): FloatArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}

internal inline fun withNullableResult(result: FloatArray, block: InteropScope.(InteropPointer) -> Boolean): FloatArray? = interopScope {
    val handle = toInteropForResult(result)
    val blockResult = block(handle)
    if (blockResult) {
        handle.fromInterop(result)
        result
    } else {
        null
    }
}

internal inline fun withResult(result: IntArray, block: InteropScope.(InteropPointer) -> Unit): IntArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}

internal inline fun withNullableResult(result: IntArray, block: InteropScope.(InteropPointer) -> Boolean): IntArray? = interopScope {
    val handle = toInteropForResult(result)
    return if (block(handle)) {
        handle.fromInterop(result)
        result
    } else {
        null
    }
}

internal inline fun withResult(result: ShortArray, block: InteropScope.(InteropPointer) -> Unit): ShortArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}

internal inline fun withResult(result: DoubleArray, block: InteropScope.(InteropPointer) -> Unit): DoubleArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}

internal inline fun withResult(result: NativePointerArray, block: InteropScope.(InteropPointer) -> Unit): NativePointerArray = interopScope {
    val handle = toInteropForResult(result)
    block(handle)
    handle.fromInterop(result)
    result
}


/**
 * Creates String from SkString* result and deletes SkString*.
 */
internal inline fun withStringResult(block: () -> NativePointer): String {
    return ManagedString(block()).use { it.toString() }
}

internal inline fun withStringResult(pointer: NativePointer): String {
    return ManagedString(pointer).use { it.toString() }
}

/**
 * Creates String from SkString* result. Caller must ensure pointer to be valid.
 * It is caller responsibility to destroy underlying SkString. Use it if pointer
 * is received from reference (SkString&)
 */
internal inline fun withStringReferenceResult(block: () -> NativePointer): String {
    val string = ManagedString(block(), false)
    return string.toString()
}

internal inline fun withStringReferenceNullableResult(block: () -> NativePointer): String? {
    val ptr = block()
    if (ptr == Native.NullPointer) return null

    val string = ManagedString(ptr, false)
    return string.toString()
}


internal interface ArrayInteropDecoder<T> {
    fun getArrayElement(array: InteropPointer, index: Int): T
    fun getArraySize(array: InteropPointer): Int
    fun disposeArray(array: InteropPointer)
}
