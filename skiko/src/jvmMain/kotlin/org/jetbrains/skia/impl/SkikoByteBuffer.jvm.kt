package org.jetbrains.skia.impl

import java.nio.ByteBuffer

actual class SkikoByteBuffer constructor(val backingByteBuffer: ByteBuffer) {

    internal actual val addr: NativePointer
        get() {
            return BufferUtil.getPointerFromByteBuffer(backingByteBuffer)
        }

    internal actual val size: Int = backingByteBuffer.capacity()

    actual constructor(
        addr: NativePointer,
        size: Int
    ): this(BufferUtil.getByteBufferFromPointer(addr, size))

    actual operator fun set(ix: Int, value: Byte) {
        backingByteBuffer.put(ix, value)
    }

    actual operator fun get(ix: Int): Byte {
        return backingByteBuffer[ix]
    }

    actual fun nativePointer(): NativePointer {
        return addr
    }
}
