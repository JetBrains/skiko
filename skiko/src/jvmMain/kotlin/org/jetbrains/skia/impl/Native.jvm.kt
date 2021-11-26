package org.jetbrains.skia.impl

import java.lang.ref.Reference

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0L
    }

    actual override fun toString(): String {
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

    // FIXME two different pointers might point to equal objects
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(_ptr)
    }

    actual open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    Reference.reachabilityFence(obj)
}

actual typealias NativePointer = Long

actual typealias InteropPointer = Any?

object theScope: InteropScope()
actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    return theScope.block()
}

actual open class InteropScope actual constructor() {
    actual fun toInterop(string: String?): InteropPointer = string
    actual fun toInterop(array: ByteArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: ByteArray) {}
    actual fun toInterop(array: ShortArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: ShortArray) {}
    actual fun toInterop(array: IntArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: IntArray) {}
    actual fun toInterop(array: LongArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: LongArray) {}
    actual fun toInterop(array: FloatArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: FloatArray) {}
    actual fun toInterop(array: DoubleArray?): InteropPointer = array
    actual fun InteropPointer.fromInterop(result: DoubleArray) {}
    actual fun toInterop(array: NativePointerArray?): InteropPointer = array?.backing
    actual fun InteropPointer.fromInterop(result: NativePointerArray) {}
    actual fun toInterop(stringArray: Array<String>?): InteropPointer = stringArray
    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray =
        NativePointerArray((this as LongArray).size, this)
    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> =
        this@fromInterop as Array<T>
    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer = interopPointers

    actual fun callback(callback: (() -> Unit)?) = callback as Any?
    actual fun booleanCallback(callback: (() -> Boolean)?) = callback as Any?

    actual fun virtual(method: () -> Unit) = callback(method)
    actual fun virtualBoolean(method: () -> Boolean) = booleanCallback(method)

    actual fun release() {}
}

actual class NativePointerArray constructor(size: Int, internal val backing: LongArray) {

    actual constructor(size: Int) : this(size, LongArray(size))

    actual operator fun get(index: Int): NativePointer {
        return backing[index]
    }

    actual operator fun set(index: Int, value: NativePointer) {
        backing[index] = value
    }

    actual val size: Int
        get() = backing.size
}
