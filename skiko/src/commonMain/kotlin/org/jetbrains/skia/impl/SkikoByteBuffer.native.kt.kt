package org.jetbrains.skia.impl

expect class SkikoByteBuffer(addr: NativePointer, size: Int) {

    internal val addr: NativePointer
    internal val size: Int

    operator fun set(ix: Int, value: Byte)
    operator fun get(ix: Int): Byte

    fun nativePointer(): NativePointer
}
