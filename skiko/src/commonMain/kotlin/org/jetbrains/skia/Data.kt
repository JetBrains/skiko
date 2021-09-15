@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Data holds an immutable data buffer.
 */
class Data internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeFromBytes(bytes: ByteArray, offset: Long = 0L, length: Long = bytes.size.toLong()): Data {
            Stats.onNativeCall()
            return Data(
                interopScope {
                    _nMakeFromBytes(toInterop(bytes), offset, length)
                }
            )
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

        init {
            staticLoad()
        }
    }

    val size: Long
        get() = try {
            Stats.onNativeCall()
            _nSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val bytes: ByteArray
        get() = getBytes(0, size)

    fun getBytes(offset: Long, length: Long): ByteArray {
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
    fun makeSubset(offset: Long, length: Long): Data {
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
            Data(_nMakeSubset(_ptr, 0, size))
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
        val PTR = Data_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Data__1nGetFinalizer")
private external fun Data_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nSize")
private external fun _nSize(ptr: NativePointer): Long

@ExternalSymbolName("org_jetbrains_skia_Data__1nBytes")
private external fun _nBytes(ptr: NativePointer, offset: Long, length: Long): ByteArray

@ExternalSymbolName("org_jetbrains_skia_Data__1nEquals")
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Data__1nToByteBuffer")
private external fun _nToByteBuffer(ptr: NativePointer): ByteBuffer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromBytes")
private external fun _nMakeFromBytes(bytes: InteropPointer, offset: Long, length: Long): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromFileName")
private external fun _nMakeFromFileName(path: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeSubset")
private external fun _nMakeSubset(ptr: NativePointer, offset: Long, length: Long): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeEmpty")
private external fun _nMakeEmpty(): NativePointer
