package org.jetbrains.skia.webext

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
suspend fun Bitmap.installPixelsFromArrayBuffer(
    info: ImageInfo,
    pixelsArrayBuffer: WebArrayBufferExt,
    rowBytes: Int
): Boolean {
    val pixelsPtr = copyBufferToSkiko(pixelsArrayBuffer)
    if (pixelsPtr == Native.NullPointer) return false
    return _nInstallPixelsFromPointer(
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