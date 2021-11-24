package org.jetbrains.skia.impl

import kotlinx.cinterop.*
import org.jetbrains.skia.ExternalSymbolName
import kotlin.native.internal.NativePtr

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other) return false
        if (other !is Native) return false
        return if (_ptr == other._ptr) true else _nativeEquals(other)
    }

    override fun hashCode(): Int {
        return _ptr.toLong().hashCode()
    }

    actual open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    actual companion object {
        init {
            initCallbacks(
                staticCFunction(::callBooleanCallback),
                staticCFunction(::callVoidCallback),
                staticCFunction(::disposeCallback),
            )
        }

        actual val NullPointer: NativePointer
            get() = NativePtr.NULL
    }

    actual override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x" + _ptr.toString() + ")"
    }

    init {
        if (ptr == NativePtr.NULL) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual typealias NativePointer = NativePtr
actual typealias InteropPointer = NativePtr

actual fun reachabilityBarrier(obj: Any?) {
    // TODO: implement native barrier
}

actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    val scope = InteropScope()
    try {
        return scope.block()
    } finally {
        scope.release()
    }
}

actual class InteropScope actual constructor() {
    actual fun toInterop(string: String?): InteropPointer {
        return if (string != null) {
            // encodeToByteArray encodes to utf8
            val utf8 = string.encodeToByteArray()
            // TODO Remove array copy, use `skString(data, length)` instead of `skString(data)`
            val pinned = utf8.copyOf(utf8.size + 1).pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun toInterop(array: CharArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: CharArray) {}

    actual fun toInterop(array: ByteArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: ByteArray) {}

    actual fun toInterop(array: ShortArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: ShortArray) {}

    actual fun toInterop(array: IntArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: IntArray) {}

    actual fun toInterop(array: LongArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: LongArray) {}

    actual fun toInterop(array: FloatArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: FloatArray) {}

    actual fun toInterop(array: DoubleArray?): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: DoubleArray) {}

    actual fun toInterop(array: NativePointerArray?): InteropPointer {
        return if (array != null && array.size > 0) {
            // We pass it as LongArray via boundary.
            val pinned = array.backing.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: NativePointerArray) {}

    actual fun toInterop(stringArray: Array<String>?): InteropPointer {
        if (stringArray == null || stringArray.isEmpty()) return NativePtr.NULL

        val pins = stringArray.toList()
            .map { it.encodeToByteArray().pin() }

        val nativePointerArray = NativePointerArray(stringArray.size)
        pins.forEachIndexed { index, pin ->
            elements.add(pin)
            nativePointerArray[index] = pin.addressOf(0).rawValue
        }
        return toInterop(nativePointerArray)
    }

    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> {
        val size = decoder.getArraySize(this)
        val result = Array<T>(size) {
            decoder.getArrayElement(this, it)
        }
        decoder.disposeArray(this)
        return result
    }

    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray {
        TODO("implement native fromInteropNativePointerArray")
    }

    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer {
        return toInterop(interopPointers.map { it.toLong() }.toLongArray())
    }

    actual fun booleanCallback(callback: (() -> Boolean)?): InteropPointer
        = callback?.let {
            val ptr = StableRef.create(it).asCPointer()
            NativePtr.NULL.plus(ptr.toLong())
        } ?: NativePtr.NULL

    actual fun callback(callback: (() -> Unit)?): InteropPointer
        = callback?.let {
            val ptr = StableRef.create(it).asCPointer()
            NativePtr.NULL.plus(ptr.toLong())
        } ?: NativePtr.NULL

    actual fun virtual(method: () -> Unit) = callback(method)
    actual fun virtualBoolean(method: () -> Boolean) = booleanCallback(method)

    actual fun release()  {
        elements.forEach {
            it.unpin()
        }
    }

    private val elements = mutableListOf<Pinned<*>>()
}

// Ugly! NativePtrArray in stdlib is unfortunately internal, don't have ctor and cannot be used.
actual class NativePointerArray actual constructor(size: Int) {
    internal val backing = LongArray(size)
    actual operator fun get(index: Int): NativePointer {
        return NativePtr.NULL + backing[index]
    }

    actual operator fun set(index: Int, value: NativePointer) {
        backing[index] = value.toLong()
    }

    actual val size: Int
        get() = backing.size
}

// Callbacks support

private fun callVoidCallback(ptr: COpaquePointer) {
    ptr.asStableRef<() -> Unit>().get().invoke()
}

private fun callBooleanCallback(ptr: COpaquePointer): Boolean {
    return ptr.asStableRef<() -> Boolean>().get().invoke()
}

private fun disposeCallback(ptr: COpaquePointer) {
    ptr.asStableRef<Any>().dispose()
}

@ExternalSymbolName("skiko_initCallbacks")
private external fun initCallbacks(callBoolean: COpaquePointer, callVoid: COpaquePointer, dispose: COpaquePointer)