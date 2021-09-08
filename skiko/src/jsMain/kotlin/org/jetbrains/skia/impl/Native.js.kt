package org.jetbrains.skia.impl

import org.jetbrains.skia.CubicResampler
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.IPoint
import org.jetbrains.skia.IRange
import org.jetbrains.skia.ImageInfo

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native?): Boolean = TODO()

    init {
        if (ptr == NULLPNTR) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    TODO()
}

actual typealias NativePointer = Int

actual val NULLPNTR: NativePointer
    get() = 0

actual fun toIRange(p: NativePointer): IRange = IRange((p ushr 32), (p and -1))
actual fun toIPoint(p: NativePointer): IPoint = IPoint((p ushr 32), (p and -1))
actual val ImageInfo.minRowBytes
    get() = (width * bytesPerPixel)

actual fun ImageInfo.computeByteSize(rowBytes: NativePointer): NativePointer {
    return if (0 == height) 0 else (height - 1) * rowBytes + width * bytesPerPixel
}

/**
 * Returns true if rowBytes is valid for this ImageInfo.
 *
 * @param rowBytes  size of pixel row including padding
 * @return          true if rowBytes is large enough to contain pixel row and is properly aligned
 */
actual fun ImageInfo.isRowBytesValid(rowBytes: NativePointer): Boolean {
    if (rowBytes < minRowBytes) return false
    val shift = shiftPerPixel
    return rowBytes shr shift shl shift == rowBytes
}

actual fun Int.toNativePointer(): NativePointer = this
actual fun CubicResampler._actualPack(): NativePointer {
    return ((b.toBits() shl 32) or c.toBits())
}

actual fun FilterMipmap._actualPack(): NativePointer {
    return filterMode.ordinal shl 32 or mipmapMode.ordinal
}