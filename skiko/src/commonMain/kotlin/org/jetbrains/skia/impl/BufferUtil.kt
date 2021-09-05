@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.impl

import org.jetbrains.skia.ByteBuffer
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

object BufferUtil {
    fun getByteBufferFromPointer(ptr: Long, size: Int): ByteBuffer {
        return _nGetByteBufferFromPointer(ptr, size)
            ?: throw IllegalArgumentException("JNI direct buffer access not support by current JVM!")
    }

    fun getPointerFromByteBuffer(buffer: ByteBuffer): Long {
        val result = _nGetPointerFromByteBuffer(buffer)
        require(result != 0L) { "The given buffer " + buffer + "is not a direct buffer or current JVM doesn't support JNI direct buffer access!" }
        return result
    }

    @JvmStatic
    @ExternalSymbolName("org_jetbrains_skia_BufferUtil__1nGetByteBufferFromPointer")
    external fun _nGetByteBufferFromPointer(ptr: Long, size: Int): ByteBuffer?
    @JvmStatic
    @ExternalSymbolName("org_jetbrains_skia_BufferUtil__1nGetPointerFromByteBuffer")
    external fun _nGetPointerFromByteBuffer(buffer: ByteBuffer?): Long
}