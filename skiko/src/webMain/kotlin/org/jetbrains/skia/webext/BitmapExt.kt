package org.jetbrains.skia.webext

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.khronos.webgl.ArrayBuffer

/**
 * Installs pixels into [Bitmap] by copying data directly from a JS [ArrayBuffer].
 *
 * This function is specific to Web targets (Kotlin/JS and Kotlin/Wasm)
 *
 * Converting an [ArrayBuffer] into an intermediate Kotlin [ByteArray] incurs extra memory allocations
 * on the Kotlin managed heap and requires multiple data-copying passes across runtime boundaries.
 *
 * For Common / Multiplatform consider the [Bitmap.installPixels], or using [org.jetbrains.skia.Data] / [org.jetbrains.skia.Image]
 */
suspend fun Bitmap.installPixelsFromArrayBuffer(
    info: ImageInfo,
    pixelsArrayBuffer: ArrayBuffer,
    rowBytes: Int
): Boolean {
    val pixelsPtr = copyBufferToSkiko(pixelsArrayBuffer)
    if (pixelsPtr == Native.NullPointer) return false
    return try {
        _imageInfo = null
        Stats.onNativeCall()
        interopScope {
            _nInstallPixelsFromPointer(
                _ptr,
                info.width,
                info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                getPtr(info.colorInfo.colorSpace),
                pixelsPtr,
                rowBytes
            )

        }
     } finally {
        reachabilityBarrier(this)
        reachabilityBarrier(info.colorInfo.colorSpace)
     }
}

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nInstallPixelsFromPointer")
private external fun _nInstallPixelsFromPointer(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPointer: NativePointer,
    rowBytes: Int
): Boolean