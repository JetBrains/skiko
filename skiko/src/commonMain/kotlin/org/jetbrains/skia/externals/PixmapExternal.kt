@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetFinalizer")
internal external fun Pixmap_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReset")
internal external fun Pixmap_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nExtractSubset")
internal external fun Pixmap_nExtractSubset(ptr: NativePointer, subsetPtr: NativePointer, l: Int, t: Int, r: Int, b: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytes")
internal external fun Pixmap_nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels")
internal external fun Pixmap_nGetRowBytesAsPixels(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeByteSize")
internal external fun Pixmap_nComputeByteSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeIsOpaque")
internal external fun Pixmap_nComputeIsOpaque(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetColor")
internal external fun Pixmap_nGetColor(ptr: NativePointer, x: Int, y: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMakeNull")
internal external fun Pixmap_nMakeNull(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMake")
internal external fun Pixmap_nMake(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nResetWithInfo")
internal external fun Pixmap_nResetWithInfo(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int
)


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nSetColorSpace")
internal external fun Pixmap_nSetColorSpace(ptr: NativePointer, colorSpacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetInfo")
internal external fun Pixmap_nGetInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddr")
internal external fun Pixmap_nGetAddr(ptr: NativePointer): NativePointer

// TODO methods flattening ImageInfo not included yet - use GetInfo() instead.

// TODO shiftPerPixel

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAlphaF")
internal external fun Pixmap_nGetAlphaF(ptr: NativePointer, x: Int, y: Int): Float

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddrAt")
internal external fun Pixmap_nGetAddrAt(ptr: NativePointer, x: Int, y: Int): NativePointer

// methods related to C++ types(addr8/16/32/64, writable_addr8/16/32/64) not included - not needed

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixels")
internal external fun Pixmap_nReadPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstPixelsPtr: NativePointer,
    dstRowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint")
internal external fun Pixmap_nReadPixelsFromPoint(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstPixelsPtr: NativePointer,
    dstRowBytes: Int,
    srcX: Int,
    srcY: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap")
internal external fun Pixmap_nReadPixelsToPixmap(ptr: NativePointer, dstPixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint")
internal external fun Pixmap_nReadPixelsToPixmapFromPoint(ptr: NativePointer, dstPixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nScalePixels")
internal external fun Pixmap_nScalePixels(ptr: NativePointer, dstPixmapPtr: NativePointer, samplingOptionsVal1: Int, samplingOptionsVal2: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nErase")
internal external fun Pixmap_nErase(ptr: NativePointer, color: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nEraseSubset")
internal external fun Pixmap_nEraseSubset(
    ptr: NativePointer,
    color: Int,
    l: Int,
    t: Int,
    r: Int,
    b: Int
): Boolean // TODO float erase methods not included
