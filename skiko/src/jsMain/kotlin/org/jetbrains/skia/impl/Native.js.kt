package org.jetbrains.skia.impl

import org.jetbrains.skia.CubicResampler
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.IPoint
import org.jetbrains.skia.IRange
import org.jetbrains.skia.ImageInfo

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native?): Boolean = TODO()

    actual companion object {
        actual val NULLPNTR: NativePointer
            get() = 0
    }

    init {
        if (ptr == NULLPNTR) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    TODO()
}

actual typealias NativePointer = Int

actual fun Int.toNativePointer(): NativePointer = this
actual fun CubicResampler._actualPack(): NativePointer {
    return ((b.toBits() shl 32) or c.toBits())
}

actual fun FilterMipmap._actualPack(): NativePointer {
    return filterMode.ordinal shl 32 or mipmapMode.ordinal
}