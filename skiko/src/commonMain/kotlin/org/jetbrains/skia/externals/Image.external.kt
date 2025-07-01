package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nGetImageInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nGetImageInfo")
internal external fun Image_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeShader")
internal external fun Image_nMakeShader(ptr: NativePointer, tmx: Int, tmy: Int, samplingModeVal1: Int, samplingModeVal2: Int, localMatrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nPeekPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nPeekPixels")
internal external fun Image_nPeekPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeRaster")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeRaster")
internal external fun Image_nMakeRaster(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixels: InteropPointer,
    rowBytes: Int
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeRasterData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeRasterData")
internal external fun Image_nMakeRasterData(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dataPtr: NativePointer,
    rowBytes: Int
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromBitmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeFromBitmap")
internal external fun Image_nMakeFromBitmap(bitmapPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeFromPixmap")
internal external fun Image_nMakeFromPixmap(pixmapPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromEncoded")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nMakeFromEncoded")
internal external fun Image_nMakeFromEncoded(bytes: InteropPointer, encodedLength: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nEncodeToData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nEncodeToData")
internal external fun Image_nEncodeToData(ptr: NativePointer, format: Int, quality: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nPeekPixelsToPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nPeekPixelsToPixmap")
internal external fun Image_nPeekPixelsToPixmap(ptr: NativePointer, pixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Image__1nScalePixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nScalePixels")
internal external fun Image_nScalePixels(ptr: NativePointer, pixmapPtr: NativePointer, samplingOptionsVal1: Int, samplingOptionsVal2: Int, cache: Boolean): Boolean

@ExternalSymbolName("org_jetbrains_skia_Image__1nReadPixelsBitmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nReadPixelsBitmap")
internal external fun Image_nReadPixelsBitmap(
    ptr: NativePointer,
    contextPtr: NativePointer,
    bitmapPtr: NativePointer,
    srcX: Int,
    srcY: Int,
    cache: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Image__1nReadPixelsPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Image__1nReadPixelsPixmap")
internal external fun Image_nReadPixelsPixmap(ptr: NativePointer, pixmapPtr: NativePointer, srcX: Int, srcY: Int, cache: Boolean): Boolean
