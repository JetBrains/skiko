package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.InternalSkikoApi
import kotlin.jvm.JvmStatic

/**
 * Data holds an immutable data buffer.
 */
class Data @InternalSkikoApi constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {

    /**
     * A reference to the underlying memory owner to prevent it from being cleaned by GC until Data instance finalization.
     *  It's used in [makeWithoutCopy].
     */
    private var underlyingMemoryOwner: Managed? = null

    companion object {
        @JvmStatic
        fun makeFromBytes(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size): Data {
            Stats.onNativeCall()
            return Data(
                interopScope {
                    _nMakeFromBytes(toInterop(bytes), offset, length)
                }
            )
        }

        /**
         * makeWithoutCopy uses NoopReleaseProc as a ReleaseProc, therefore memory needs to be cleaned up by caller side
         * (or by [underlyingMemoryOwner] if provided).
         *
         * @param underlyingMemoryOwner - is stored in Data instance to prevent underlying memory getting cleaned by GC in cases when
         * [underlyingMemoryOwner] doesn't have any other references.
         */
        @JvmStatic
        fun makeWithoutCopy(memoryAddr: NativePointer, length: Int, underlyingMemoryOwner: Managed): Data {
            Stats.onNativeCall()
            return Data(
                interopScope {
                    _nMakeWithoutCopy(memoryAddr, length)
                }
            ).also {
                it.underlyingMemoryOwner = underlyingMemoryOwner
            }
        }

        /**
         * Returns a new empty dataref (or a reference to a shared empty dataref).
         * New or shared, the caller must see that [.close] is eventually called.
         */
        @JvmStatic
        fun makeEmpty(): Data {
            Stats.onNativeCall()
            return Data(_nMakeEmpty())
        }

        @JvmStatic
        fun makeUninitialized(length: Int): Data {
            Stats.onNativeCall()
            return Data(_nMakeUninitialized(length))
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
        return nativeEquals(otherData)
    }

    /**
     * Returns true if these two objects have the same length and contents,
     * effectively returning 0 == memcmp(...)
     */
    override fun nativeEquals(other: Native?): Boolean {
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

    fun writableData(): NativePointer {
        return try {
            Stats.onNativeCall()
            _nWritableData(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = Data_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_Data__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of an unref function without calling it, so it is short and cannot
// block.
// Native call path: _nGetFinalizer
private external fun Data_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nSize")
// No ForceNativeThreadState: returns the stored byte length, so it is short and cannot block.
// Native call path: SkData::size
private external fun _nSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Data__1nBytes")
// Needs ForceNativeThreadState: copies a caller-chosen number of bytes out, so its time grows with that length.
// Native call path: memcpy
@ForceNativeThreadState
private external fun _nBytes(ptr: NativePointer, offset: Int, length: Int, destBytes: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Data__1nEquals")
// Needs ForceNativeThreadState: compares every byte of the data, so its time grows with the data size.
// Native call path: SkData::equals -> sk_careful_memcmp
@ForceNativeThreadState
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromBytes")
// Needs ForceNativeThreadState: allocates and copies the whole byte array, so its time grows with the array length.
// Native call path: memcpy
@ForceNativeThreadState
private external fun _nMakeFromBytes(bytes: InteropPointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeWithoutCopy")
// No ForceNativeThreadState: wraps an existing pointer in a new data object without copying the bytes, so it is short
// and cannot block.
// Native call path: SkData::MakeWithoutCopy -> MakeWithProc
private external fun _nMakeWithoutCopy(memoryAddr: NativePointer, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromFileName")
// Needs ForceNativeThreadState: opens and maps a file, which is blocking file I/O.
// Native call path: SkData::MakeFromFileName -> MakeFromFILE -> sk_fmmap
@ForceNativeThreadState
internal external fun _nMakeFromFileName(path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeSubset")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty data object.
// Native call path: SkData::MakeSubset -> SkData::MakeEmpty
@ForceNativeThreadState
private external fun _nMakeSubset(ptr: NativePointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeEmpty")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty data object on first
// use.
// Native call path: SkData::MakeEmpty
@ForceNativeThreadState
private external fun _nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeUninitialized")
// Needs ForceNativeThreadState: for zero length, can wait for another thread still initializing the shared empty data
// object.
// Native call path: SkData::MakeUninitialized -> PrivateNewWithCopy -> SkData::MakeEmpty
@ForceNativeThreadState
private external fun _nMakeUninitialized(length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nWritableData")
// No ForceNativeThreadState: returns the stored data pointer, so it is short and cannot block.
// Native call path: SkData::writable_data
private external fun _nWritableData(dataPtr: NativePointer): NativePointer
