package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

actual val ImageInfo.minRowBytes
    get() = (width * bytesPerPixel).toLong()

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
