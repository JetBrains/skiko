package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGetWidth")
internal external fun Surface_nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGetHeight")
internal external fun Surface_nGetHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetImageInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGetImageInfo")
internal external fun Surface_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nReadPixels")
internal external fun Surface_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nWritePixels")
internal external fun Surface_nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterDirect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeRasterDirect")
internal external fun Surface_nMakeRasterDirect(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int,
    surfaceProps: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap")
internal external fun Surface_nMakeRasterDirectWithPixmap(pixmapPtr: NativePointer, surfaceProps: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRaster")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeRaster")
internal external fun Surface_nMakeRaster(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int,
    surfaceProps: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterN32Premul")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeRasterN32Premul")
internal external fun Surface_nMakeRasterN32Premul(width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget")
internal external fun Surface_nMakeFromBackendRenderTarget(
    pContext: NativePointer,
    pBackendRenderTarget: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeFromMTKView")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeFromMTKView")
internal external fun Surface_nMakeFromMTKView(
    contextPtr: NativePointer,
    mtkViewPtr: NativePointer,
    surfaceOrigin: Int,
    sampleCount: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRenderTarget")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeRenderTarget")
internal external fun Surface_nMakeRenderTarget(
    contextPtr: NativePointer,
    budgeted: Boolean,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    sampleCount: Int,
    surfaceOrigin: Int,
    surfaceProps: InteropPointer,
    shouldCreateWithMips: Boolean
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeNull")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeNull")
internal external fun Surface_nMakeNull(width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGenerationId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGenerationId")
internal external fun Surface_nGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nNotifyContentWillChange")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nNotifyContentWillChange")
internal external fun Surface_nNotifyContentWillChange(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetRecordingContext")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGetRecordingContext")
internal external fun Surface_nGetRecordingContext(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetCanvas")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nGetCanvas")
internal external fun Surface_nGetCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurfaceI")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeSurfaceI")
internal external fun Surface_nMakeSurfaceI(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeSurface")
internal external fun Surface_nMakeSurface(ptr: NativePointer, width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshot")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeImageSnapshot")
internal external fun Surface_nMakeImageSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshotR")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nMakeImageSnapshotR")
internal external fun Surface_nMakeImageSnapshotR(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nDraw")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nDraw")
internal external fun Surface_nDraw(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float, samplingModeValue1: Int, samplingModeValue2: Int, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nPeekPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nPeekPixels")
internal external fun Surface_nPeekPixels(ptr: NativePointer, pixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixelsToPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nReadPixelsToPixmap")
internal external fun Surface_nReadPixelsToPixmap(ptr: NativePointer, pixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixelsFromPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nWritePixelsFromPixmap")
internal external fun Surface_nWritePixelsFromPixmap(ptr: NativePointer, pixmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nUnique")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Surface__1nUnique")
internal external fun Surface_nUnique(ptr: NativePointer): Boolean
