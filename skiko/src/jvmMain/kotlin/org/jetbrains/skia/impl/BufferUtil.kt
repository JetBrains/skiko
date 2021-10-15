package org.jetbrains.skia.impl

import java.nio.ByteBuffer

internal object BufferUtil {
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


private external fun _nGetByteBufferFromPointer(ptr: NativePointer, size: Int): ByteBuffer?

private external fun _nGetPointerFromByteBuffer(buffer: ByteBuffer?): NativePointer
