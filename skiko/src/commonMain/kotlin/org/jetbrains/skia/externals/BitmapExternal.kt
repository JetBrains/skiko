@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetFinalizer")
internal external fun Bitmap_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMake")
internal external fun Bitmap_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeClone")
internal external fun Bitmap_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSwap")
internal external fun Bitmap_nSwap(ptr: NativePointer, otherPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetImageInfo")
internal external fun Bitmap_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels")
internal external fun Bitmap_nGetRowBytesAsPixels(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsNull")
internal external fun Bitmap_nIsNull(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytes")
internal external fun Bitmap_nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetAlphaType")
internal external fun Bitmap_nSetAlphaType(ptr: NativePointer, alphaType: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeByteSize")
internal external fun Bitmap_nComputeByteSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsImmutable")
internal external fun Bitmap_nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImmutable")
internal external fun Bitmap_nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReset")
internal external fun Bitmap_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeIsOpaque")
internal external fun Bitmap_nComputeIsOpaque(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImageInfo")
internal external fun Bitmap_nSetImageInfo(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixelsFlags")
internal external fun Bitmap_nAllocPixelsFlags(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    flags: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixelsRowBytes")
internal external fun Bitmap_nAllocPixelsRowBytes(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nInstallPixels")
internal external fun Bitmap_nInstallPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixels: InteropPointer,
    rowBytes: Int,
    pixelsLen: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixels")
internal external fun Bitmap_nAllocPixels(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRef")
internal external fun Bitmap_nGetPixelRef(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginX")
internal external fun Bitmap_nGetPixelRefOriginX(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginY")
internal external fun Bitmap_nGetPixelRefOriginY(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetPixelRef")
internal external fun Bitmap_nSetPixelRef(ptr: NativePointer, pixelRefPtr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsReadyToDraw")
internal external fun Bitmap_nIsReadyToDraw(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetGenerationId")
internal external fun Bitmap_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged")
internal external fun Bitmap_nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nEraseColor")
internal external fun Bitmap_nEraseColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nErase")
internal external fun Bitmap_nErase(ptr: NativePointer, color: Int, left: Int, top: Int, right: Int, bottom: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetColor")
internal external fun Bitmap_nGetColor(ptr: NativePointer, x: Int, y: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetAlphaf")
internal external fun Bitmap_nGetAlphaf(ptr: NativePointer, x: Int, y: Int): Float

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nExtractSubset")
internal external fun Bitmap_nExtractSubset(ptr: NativePointer, dstPtr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReadPixels")
internal external fun Bitmap_nReadPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstRowBytes: Int,
    srcX: Int,
    srcY: Int,
    resultBytes: InteropPointer
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nExtractAlpha")
internal external fun Bitmap_nExtractAlpha(ptr: NativePointer, dstPtr: NativePointer, paintPtr: NativePointer, iPointResultIntArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nPeekPixels")
internal external fun Bitmap_nPeekPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeShader")
internal external fun Bitmap_nMakeShader(ptr: NativePointer, tmx: Int, tmy: Int, samplingModeValue1: Int, samplingModeValue2: Int, localMatrix: InteropPointer): NativePointer
