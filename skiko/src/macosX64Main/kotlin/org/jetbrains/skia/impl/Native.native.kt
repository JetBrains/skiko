package org.jetbrains.skia.impl

import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlin.native.internal.NativePtr

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer
    override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x$_ptr)"
    }

    actual open fun _nativeEquals(other: Native?): Boolean = this._ptr == other?._ptr ?: Native.NullPointer

    actual companion object {
        actual val NullPointer: NativePointer
            get() = NativePtr.NULL
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

actual class InteropScope actual constructor() {
    actual fun toInterop(array: ByteArray?): InteropPointer {
        return if (array != null) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: ByteArray) {}

    actual fun toInterop(array: FloatArray?): InteropPointer {
        return if (array != null) {
            val pinned = array.pin()
            elements.add(pinned)
            val result = pinned.addressOf(0).rawValue
            result
        } else {
            NativePtr.NULL
        }
    }

    actual fun InteropPointer.fromInterop(result: FloatArray) {}

    actual fun release()  {
        elements.forEach {
            it.unpin()
        }
    }

    private val elements = mutableListOf<Pinned<*>>()
}

