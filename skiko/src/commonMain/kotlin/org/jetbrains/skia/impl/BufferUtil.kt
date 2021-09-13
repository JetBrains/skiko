@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.impl

import org.jetbrains.skia.ByteBuffer
import org.jetbrains.skia.ExternalSymbolName

object BufferUtil {
    fun getByteBufferFromPointer(ptr: NativePointer, size: Int): ByteBuffer {
        return _nGetByteBufferFromPointer(ptr, size)
            ?: throw IllegalArgumentException("JNI direct buffer access not support by current JVM!")
    }

    fun getPointerFromByteBuffer(buffer: ByteBuffer): NativePointer {
        val result = _nGetPointerFromByteBuffer(buffer)
        require(result != Native.NullPointer) { "The given buffer " + buffer + "is not a direct buffer or current JVM doesn't support JNI direct buffer access!" }
        return result
    }
}


@ExternalSymbolName("org_jetbrains_skia_BufferUtil__1nGetByteBufferFromPointer")
private external fun _nGetByteBufferFromPointer(ptr: NativePointer, size: Int): ByteBuffer?

@ExternalSymbolName("org_jetbrains_skia_BufferUtil__1nGetPointerFromByteBuffer")
private external fun _nGetPointerFromByteBuffer(buffer: ByteBuffer?): NativePointer