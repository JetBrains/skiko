package org.jetbrains.skia.impl

actual class SkikoByteBuffer actual constructor(
    internal actual val addr: NativePointer,
    internal actual val size: Int
) {

    actual operator fun set(ix: Int, value: Byte) {
        val actualIndex = addr + ix
        js("HEAP8[actualIndex] = value")
    }

    actual operator fun get(ix: Int): Byte {
        val actualIndex = addr + ix
        return js("HEAP8[actualIndex]") as Byte
    }

    actual fun nativePointer(): NativePointer = addr
}
