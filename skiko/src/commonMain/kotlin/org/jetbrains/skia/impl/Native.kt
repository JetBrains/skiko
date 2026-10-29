package org.jetbrains.skia.impl

import org.jetbrains.skia.ManagedString
import org.jetbrains.skiko.InternalSkikoApi

expect class NativePointer

@InternalSkikoApi
expect class InteropPointer

expect abstract class Native(ptr: NativePointer) {
    internal var _ptr: NativePointer
    @InternalSkikoApi
    val nativePtr: NativePointer
    internal open fun nativeEquals(other: Native?): Boolean

    companion object {
        val NullPointer: NativePointer
    }

    override fun toString(): String
}

@InternalSkikoApi
expect fun reachabilityBarrier(obj: Any?)


/**
 * Use only when [Native] is null. If not, use [Native._ptr] for less overhead.
 */
@InternalSkikoApi
val Native?.pointerOrNullPointer: NativePointer
    get() = this?._ptr ?: Native.NullPointer

/**
 * Make a native call with the own class it is called from.
 */
@InternalSkikoApi
inline fun <T, R> T.nativeCall(crossinline nativeCall: T.() -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall()
    } finally {
        reachabilityBarrier(this)
    }

/**
 * Make a native call with the own class it is called from and an additional object.
 */
@InternalSkikoApi
inline fun <T, O, R> T.nativeCall(obj : O, crossinline nativeCall: T.(O) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj)
    } finally {
        reachabilityBarrier(this)
        reachabilityBarrier(obj)
    }

/**
 * Make a native call with the own class it is called from and two additional objects.
 */
@InternalSkikoApi
inline fun <T, O1, O2, R> T.nativeCall(obj1 : O1, obj2 : O2, crossinline nativeCall: T.(O1, O2) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj1, obj2)
    } finally {
        reachabilityBarrier(this)
        reachabilityBarrier(obj1)
        reachabilityBarrier(obj2)
    }


@InternalSkikoApi
inline fun <R> nativeCallFromStatic(crossinline nativeCall: () -> R): R =
    run {
        Stats.onNativeCall()
        nativeCall()
    }

@InternalSkikoApi
inline fun <O, R> nativeCallFromStatic(obj : O, crossinline nativeCall:(O) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj)
    } finally {
        reachabilityBarrier(obj)
    }

@InternalSkikoApi
inline fun <O1, O2, R> nativeCallFromStatic(obj1 : O1, obj2 : O2, crossinline nativeCall: (O1, O2) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj1, obj2)
    } finally {
        reachabilityBarrier(obj1)
        reachabilityBarrier(obj2)
    }

@InternalSkikoApi
inline fun <O1, O2, O3, R> nativeCallFromStatic(obj1 : O1, obj2 : O2, obj3 : O3, crossinline nativeCall: (O1, O2, O3) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj1, obj2, obj3)
    } finally {
        reachabilityBarrier(obj1)
        reachabilityBarrier(obj2)
        reachabilityBarrier(obj3)
    }

@InternalSkikoApi
inline fun <O1, O2, O3, O4, R> nativeCallFromStatic(obj1 : O1, obj2 : O2, obj3 : O3, obj4 : O4, crossinline nativeCall: (O1, O2, O3, O4) -> R): R =
    try {
        Stats.onNativeCall()
        nativeCall(obj1, obj2, obj3, obj4)
    } finally {
        reachabilityBarrier(obj1)
        reachabilityBarrier(obj2)
        reachabilityBarrier(obj3)
        reachabilityBarrier(obj4)
    }

@InternalSkikoApi
expect class InteropScope() {
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

@InternalSkikoApi
expect inline fun <T> interopScope(block: InteropScope.() -> T): T

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

@Suppress("NOTHING_TO_INLINE")
internal inline fun withStringResult(pointer: NativePointer): String {
    return ManagedString(pointer).use { it.toString() }
}

/**
 * Creates String from SkString* result. Caller must ensure pointer to be valid.
 * It is caller responsibility to destroy underlying SkString. Use it if pointer
 * is received from reference (SkString&)
 */
@InternalSkikoApi
fun withStringReferenceResult(block: () -> NativePointer): String {
    val string = ManagedString(block(), false)
    return string.toString()
}

@InternalSkikoApi
fun withStringReferenceNullableResult(block: () -> NativePointer): String? {
    val ptr = block()
    if (ptr == Native.NullPointer) return null

    val string = ManagedString(ptr, false)
    return string.toString()
}


@InternalSkikoApi
interface ArrayInteropDecoder<T> {
    fun getArrayElement(array: InteropPointer, index: Int): T
    fun getArraySize(array: InteropPointer): Int
    fun disposeArray(array: InteropPointer)
}
