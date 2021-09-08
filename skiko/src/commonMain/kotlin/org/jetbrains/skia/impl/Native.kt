package org.jetbrains.skia.impl

import org.jetbrains.skia.CubicResampler
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.IPoint
import org.jetbrains.skia.IRange
import org.jetbrains.skia.ImageInfo

expect class NativePointer
expect val NULLPNTR: NativePointer


expect fun toIPoint(p: NativePointer): IPoint
expect fun toIRange(p: NativePointer): IRange

/**
 * Returns minimum bytes per row, computed from pixel getWidth() and ColorType, which
 * specifies getBytesPerPixel(). Bitmap maximum value for row bytes must fit
 * in 31 bits.
 */
expect val ImageInfo.minRowBytes: NativePointer

/**
 *
 * Returns storage required by pixel array, given ImageInfo dimensions, ColorType,
 * and rowBytes. rowBytes is assumed to be at least as large as [.getMinRowBytes].
 *
 *
 * Returns zero if height is zero.
 *
 * @param rowBytes  size of pixel row or larger
 * @return          memory required by pixel buffer
 *
 * @see [https://fiddle.skia.org/c/@ImageInfo_computeByteSize](https://fiddle.skia.org/c/@ImageInfo_computeByteSize)
 */
expect fun ImageInfo.computeByteSize(rowBytes: NativePointer): NativePointer

/**
 * Returns true if rowBytes is valid for this ImageInfo.
 *
 * @param rowBytes  size of pixel row including padding
 * @return          true if rowBytes is large enough to contain pixel row and is properly aligned
 */
expect fun ImageInfo.isRowBytesValid(rowBytes: NativePointer): Boolean

expect fun Int.toNativePointer(): NativePointer

expect fun CubicResampler._actualPack(): NativePointer
expect fun FilterMipmap._actualPack(): NativePointer

expect abstract class Native(ptr: NativePointer) {
    var _ptr: NativePointer
    open fun _nativeEquals(other: Native?): Boolean
}

expect fun reachabilityBarrier(obj: Any?)

fun getPtr(n: Native?): NativePointer = n?._ptr ?: NULLPNTR

