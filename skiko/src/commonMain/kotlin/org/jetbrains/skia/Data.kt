package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * Data holds an immutable data buffer.
 */
class Data internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeFromBytes(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size): Data {
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
            interopScope {
                return Data(_nMakeFromFileName(toInterop(path)))
            }
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

    val size: Int
        get() = try {
            Stats.onNativeCall()
            _nSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val bytes: ByteArray
        get() = getBytes(0, size)

    fun getBytes(offset: Int, length: Int): ByteArray {
        return try {
            Stats.onNativeCall()
            check(_nSize(_ptr) >= offset + length) {
                "Data=${_ptr}: Can't getBytes with offset=$offset and length=$length"
            }
            withResult(ByteArray(length)) {
                _nBytes(_ptr, offset, length, it)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        val otherData = other as? Data ?: return false
        return _nativeEquals(otherData)
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
    fun makeSubset(offset: Int, length: Int): Data {
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
private external fun _nSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Data__1nBytes")
private external fun _nBytes(ptr: NativePointer, offset: Int, length: Int, destBytes: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Data__1nEquals")
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Data__1nToByteBuffer")
private external fun _nToByteBuffer(ptr: NativePointer): ByteBuffer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromBytes")
private external fun _nMakeFromBytes(bytes: InteropPointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromFileName")
private external fun _nMakeFromFileName(path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeSubset")
private external fun _nMakeSubset(ptr: NativePointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeEmpty")
private external fun _nMakeEmpty(): NativePointer
