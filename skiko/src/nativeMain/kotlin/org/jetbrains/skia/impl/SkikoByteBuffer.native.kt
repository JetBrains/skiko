package org.jetbrains.skia.impl

import kotlinx.cinterop.*

actual class SkikoByteBuffer actual constructor(
    internal actual val addr: NativePointer,
    internal actual val size: Int
) {

    actual operator fun set(ix: Int, value: Byte) {
        addr.toLong().toCPointer<ByteVar>()!![ix] = value
    }

    actual operator fun get(ix: Int): Byte {
        return addr.toLong().toCPointer<ByteVar>()!![ix]
    }

    actual fun nativePointer(): NativePointer = addr
}
