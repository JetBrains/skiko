@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.toNativePointer
import kotlin.jvm.JvmStatic

/**
 * Data holds an immutable data buffer.
 */
class Data internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeFromBytes(bytes: ByteArray, offset: NativePointer = NULLPNTR, length: NativePointer = bytes.size.toNativePointer()): Data {
            Stats.onNativeCall()
            return Data(_nMakeFromBytes(bytes, offset, length))
        }

        /**
         * Create a new dataref the file with the specified path.
         * If the file cannot be opened, this returns null.
         */
        fun makeFromFileName(path: String?): Data {
            Stats.onNativeCall()
            return Data(_nMakeFromFileName(path))
        }

        /**
         * Returns a new empty dataref (or a reference to a shared empty dataref).
         * New or shared, the caller must see that [.close] is eventually called.
         */
        fun makeEmpty(): Data {
            Stats.onNativeCall()
            return Data(_nMakeEmpty())
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nSize")
        external fun _nSize(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nBytes")
        external fun _nBytes(ptr: NativePointer, offset: NativePointer, length: NativePointer): ByteArray
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nEquals")
        external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nToByteBuffer")
        external fun _nToByteBuffer(ptr: NativePointer): ByteBuffer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromBytes")
        external fun _nMakeFromBytes(bytes: ByteArray?, offset: NativePointer, length: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromFileName")
        external fun _nMakeFromFileName(path: String?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nMakeSubset")
        external fun _nMakeSubset(ptr: NativePointer, offset: NativePointer, length: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Data__1nMakeEmpty")
        external fun _nMakeEmpty(): NativePointer

        init {
            staticLoad()
        }
    }

    val size: NativePointer
        get() = try {
            Stats.onNativeCall()
            _nSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val bytes: ByteArray
        get() = getBytes(NULLPNTR, size)

    fun getBytes(offset: NativePointer, length: NativePointer): ByteArray {
        return try {
            Stats.onNativeCall()
            _nBytes(_ptr, offset, length)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns true if these two objects have the same length and contents,
     * effectively returning 0 == memcmp(...)
     */
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     * Create a new dataref using a subset of the data in the specified
     * src dataref.
     */
    fun makeSubset(offset: NativePointer, length: NativePointer): Data {
        return try {
            Stats.onNativeCall()
            Data(_nMakeSubset(_ptr, offset, length))
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun makeCopy(): Data {
        return try {
            Stats.onNativeCall()
            Data(_nMakeSubset(_ptr, NULLPNTR, size))
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun toByteBuffer(): ByteBuffer {
        return try {
            Stats.onNativeCall()
            _nToByteBuffer(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}
