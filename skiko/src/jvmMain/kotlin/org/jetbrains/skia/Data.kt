package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference
import java.nio.ByteBuffer

/**
 * Data holds an immutable data buffer.
 */
class Data internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmOverloads
        fun makeFromBytes(bytes: ByteArray, offset: Long = 0, length: Long = bytes.size.toLong()): Data {
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

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nSize(ptr: Long): Long
        @JvmStatic external fun _nBytes(ptr: Long, offset: Long, length: Long): ByteArray
        @JvmStatic external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic external fun _nToByteBuffer(ptr: Long): ByteBuffer
        @JvmStatic external fun _nMakeFromBytes(bytes: ByteArray?, offset: Long, length: Long): Long
        @JvmStatic external fun _nMakeFromFileName(path: String?): Long
        @JvmStatic external fun _nMakeSubset(ptr: Long, offset: Long, length: Long): Long
        @JvmStatic external fun _nMakeEmpty(): Long

        init {
            staticLoad()
        }
    }

    val size: Long
        get() = try {
            Stats.onNativeCall()
            _nSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val bytes: ByteArray
        get() = getBytes(0, size)

    fun getBytes(offset: Long, length: Long): ByteArray {
        return try {
            Stats.onNativeCall()
            _nBytes(_ptr, offset, length)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Returns true if these two objects have the same length and contents,
     * effectively returning 0 == memcmp(...)
     */
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, Native.Companion.getPtr(other))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
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
            Reference.reachabilityFence(this)
        }
    }

    fun makeCopy(): Data {
        return try {
            Stats.onNativeCall()
            Data(_nMakeSubset(_ptr, 0, size))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun toByteBuffer(): ByteBuffer {
        return try {
            Stats.onNativeCall()
            _nToByteBuffer(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}