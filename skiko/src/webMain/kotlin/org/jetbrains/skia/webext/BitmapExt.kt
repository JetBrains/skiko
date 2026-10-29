package org.jetbrains.skia.webext

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.*
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
    return nativeCall(info.colorInfo.colorSpace) { colorSpace ->
        _imageInfo = null
        interopScope {
            _nInstallPixelsFromPointer(
                _ptr,
                info.width,
                info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                colorSpace.pointerOrNullPointer,
                pixelsPtr,
                rowBytes
            )

        }
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